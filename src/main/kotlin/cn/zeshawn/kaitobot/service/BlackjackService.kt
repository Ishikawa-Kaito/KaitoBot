package cn.zeshawn.kaitobot.service


import cn.zeshawn.kaitobot.service.BlackJackGame.Companion.getPlayer
import io.ktor.util.*
import io.ktor.util.collections.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain
import java.lang.Integer.min
import java.util.concurrent.ConcurrentLinkedQueue

@OptIn(KtorExperimentalAPI::class)
object BlackjackService {


    private val games = ConcurrentList<BlackJackGame>()

    suspend fun start(event: GroupMessageEvent) {
        if (games.any { it.id == event.subject.id }) {
            event.subject.sendMessage("游戏正在进行中。")
            return
        }
        event.subject.sendMessage("二十一点游戏，请在30秒内输入“下注 [数字]” 参加游戏。")
        val game = BlackJackGame(event.subject.id)
        games.add(game)
        closeNullGame(event, game)
        print(1)
    }

    private suspend fun closeNullGame(event: GroupMessageEvent, game: BlackJackGame) {
        delay(30 * 1000)
        if (game.phase == BlackJackGamePhase.CALLING) {
            event.subject.sendMessage("本局游戏已取消。")
            games.remove(game)
            return
        }

    }

    suspend fun handle(event: GroupMessageEvent) {
        val message = event.message.contentToString()
        when {
            message == "要牌" -> deal(event)
            message == "停牌" -> fold(event)
            "下注" in message -> bet(event)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun bet(event: GroupMessageEvent) {
        val id = event.sender.id
        val game = games.find { it.id == event.subject.id } ?: return
        val message = event.message.contentToString()
        val bets = message.replace("下注", "").trim().toLong()
        if (bets <= 0) {
            event.subject.sendMessage("你莫不是特地来消遣洒家？")
            return
        }
        if (BankService.purchaseDCoin(id, bets)) {
            event.subject.sendMessage(
                buildMessageChain {
                    +"已收到"
                    +At(id)
                    +" $bets 斗币下注。"
                }
            )
            game.bet(id, bets)
            if (game.phase == BlackJackGamePhase.CALLING) {
                game.phase = BlackJackGamePhase.BET
                GlobalScope.launch {
                    betPhase(event, game)
                }
            }
        }
    }

    private suspend fun deal(event: GroupMessageEvent) {
        val id = event.sender.id
        val game = games.find { it.id == event.subject.id } ?: return
        val player = game.players.getPlayer(id) ?: return
        if (!player.canOperate) return
        val card = game.deal()
        event.subject.sendMessage(buildMessageChain {
            +At(id)
            +"你抽到了一张 $card"
        })
        if (!player.draw(listOf(card))) {
            event.subject.sendMessage(buildMessageChain {
                +At(id)
                +"爆牌了。"
            })
            player.canOperate = false
        }
        checkAllFold(event)
    }

    private suspend fun fold(event: GroupMessageEvent) {
        val id = event.sender.id
        val game = games.find { it.id == event.subject.id } ?: return
        val player = game.players.getPlayer(id) ?: return
        if (!player.canOperate) return
        game.fold(id)
        event.subject.sendMessage(buildMessageChain {
            +At(id)
            +"停牌了。"
        })
        checkAllFold(event)
    }

    private suspend fun betPhase(event: GroupMessageEvent, game: BlackJackGame) {
        delay(30 * 1000)
        if (game.phase == BlackJackGamePhase.BET) {
            game.endBetPhase()
            event.subject.sendMessage("下注阶段已结束，请在30秒内进行操作。")
            event.subject.sendMessage(
                buildMessageChain {
                    +"庄家摸到的牌是：\n${game.bookmaker.cards[0]} 暗牌\n"
                    game.players.forEach {
                        +At(it.id)
                        +"\n摸到的牌是：\n${it.cards[0]} ${it.cards[1]}"
                    }
                }
            )
            opPhase(event, game)
        }
    }

    private suspend fun opPhase(event: GroupMessageEvent, game: BlackJackGame) {
        delay(30 * 1000)
        if (game.phase == BlackJackGamePhase.OP) {
            endGame(event, game)
        }
    }

    private suspend fun checkAllFold(event: GroupMessageEvent) {
        val game = games.find { it.id == event.subject.id } ?: return
        if (game.checkAllFold()) {
            endGame(event, game)
        }
    }

    private suspend fun endGame(event: GroupMessageEvent, game: BlackJackGame) {
        game.endOpPhase()
        game.bookmakerOp()
        event.subject.sendMessage("本局游戏结束。")
        games.remove(game)
        val result = game.getPoint()
        event.subject.sendMessage(
            buildMessageChain {
                +"庄家的牌是：\n"
                game.bookmaker.cards.forEach {
                    +(" $it")
                }
                +"\n"
                result.forEach {
                    +"\n"
                    +At(it.key)
                    +"获得了 ${it.value} 斗币。"
                    BankService.addDCoin(it.key, it.value)
                }
            }
        )
    }

}

@OptIn(KtorExperimentalAPI::class)
class BlackJackGame(val id: Long) {

    private val cardPile = ConcurrentLinkedQueue(getCardPile())

    val bookmaker = BlackJackPlayer(0, 0)

    var phase = BlackJackGamePhase.CALLING

    val players = ConcurrentList<BlackJackPlayer>()

    private fun getCardPile(deckNum: Int = 4): MutableList<PokerCard> {
        return buildList {
            for (d in 0 until deckNum) {
                for (i in 0 until 52) {
                    val index = i / 4
                    val suit = Suit.getFromIndex(i % 4)
                    add(PokerCard(index, suit))
                }
            }
        }.shuffled().toMutableList()
    }


    fun bet(id: Long, bets: Long) {
        if (players.getPlayer(id) == null) {
            players.add(BlackJackPlayer(id, bets))
        } else {
            players.getPlayer(id)!!.bet += bets
        }
    }

    private fun dealCards() {
        // 玩家摸
        players.forEach {
            val cards = listOf(cardPile.poll(), cardPile.poll())
            it.draw(cards)
        }
        //庄家摸
        bookmaker.draw(listOf(cardPile.poll(), cardPile.poll()))
    }

    fun deal(): PokerCard {
        return cardPile.poll()
    }

    fun fold(id: Long) {
        this.players.getPlayer(id)!!.canOperate = false
    }

    fun endOpPhase() {
        this.phase = BlackJackGamePhase.END
    }

    fun endBetPhase() {
        this.phase = BlackJackGamePhase.OP
        dealCards()
        players.forEach { it.canOperate = true }
    }

    fun checkAllFold(): Boolean {
        return players.all { !it.canOperate }
    }

    fun bookmakerOp() {
        while (bookmaker.calcPoint() < 17) {
            bookmaker.draw(listOf(cardPile.poll()))
        }
    }

    fun getPoint(): Map<Long, Long> {
        return buildMap {
            players.forEach {
                val bonus = if (it.isBusted) {
                    0
                } else if (bookmaker.isBusted) {
                    it.bet * 2
                } else {
                    when {
                        it.calcPoint() > bookmaker.calcPoint() -> it.bet * 2
                        it.calcPoint() == bookmaker.calcPoint() -> it.bet
                        it.calcPoint() < bookmaker.calcPoint() -> 0
                        else -> 0
                    }
                }
                put(it.id, bonus)
            }
        }
    }


    companion object {
        fun ConcurrentList<BlackJackPlayer>.getPlayer(id: Long): BlackJackPlayer? {
            this.forEach {
                if (it.id == id) return it
            }
            return null
        }
    }
}

enum class BlackJackGamePhase {
    CALLING,
    BET,
    OP,
    END
}

class BlackJackPlayer(val id: Long, var bet: Long = 0) {

    val cards = mutableListOf<PokerCard>()

    var canOperate = false

    val isBusted: Boolean
        get() {
            return calcPoint() > 21
        }

    fun draw(cards: Iterable<PokerCard>): Boolean {
        this.cards.addAll(cards)
        return this.calcPoint() > 21
    }

    fun calcPoint(): Int {
        var sum = 0
        for (card in cards.sortedByDescending { it.point }) {
            sum += if (card.point == 1) {
                if (sum + 11 <= 21) {
                    11
                } else 1
            } else card.point
        }
        return sum
    }

    override fun equals(other: Any?): Boolean {
        if (other !is BlackJackPlayer) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}


class PokerCard(private val index: Int, val suit: Suit) {

    val point: Int
        get() {
            return min(index + 1, 10)
        }

    companion object {
        val cardFaces = "A 2 3 4 5 6 7 8 9 10 J Q K".split(" ")
    }

    override fun toString(): String {
        return suit.toString() + this.getCardFace()
    }

    private fun getCardFace(): String {
        return cardFaces[this.index]
    }

    override operator fun equals(other: Any?): Boolean {
        if (other !is PokerCard) return false
        return this.point == other.point && this.suit == other.suit
    }


    override fun hashCode(): Int {
        return toString().hashCode()
    }
}


enum class Suit {
    Club,
    Diamond,
    Heart,
    Spade;

    override fun toString(): String {
        return when (this) {
            Club -> "♣"
            Diamond -> "♦"
            Heart -> "♥"
            Spade -> "♠"
        }
    }

    companion object {
        fun getFromIndex(index: Int): Suit {
            return when (index) {
                0 -> Club
                1 -> Diamond
                2 -> Heart
                3 -> Spade
                else -> throw IllegalArgumentException()
            }
        }
    }

}


