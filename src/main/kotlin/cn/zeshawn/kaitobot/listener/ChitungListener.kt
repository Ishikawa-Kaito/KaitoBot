package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import java.time.Instant
import kotlin.random.Random

object ChitungListener : IListener {
    override val name: String
        get() = "七筒！！！"


    private val idiotLoveWords =
        listOf(
            "七筒老婆！！！！！\uD83D\uDC8B\uD83D\uDC98\uD83D\uDC9D\uD83D\uDC8C\uD83D\uDE0D\uD83D\uDC93\uD83E\uDD1F\n我是你的舔狗啊！！！",
            "筒宝，每次跟你打LOL，我总希望对面有人来偷水晶，你会说我们家被偷了，这时我就很开心，原来我可以跟你有个家。",
            "今天晚上有点冷，本来以为街上没有人，结果刚刚偷电瓶的时候被抓，本来想反抗，结果警察说了一句老实点别动，我立刻就放弃了抵抗，因为我记得七筒说过他喜欢老实人。",
            "筒宝，以后别叫我舔狗了，我军训完了，以后就叫我军犬吧。",
            "心都碎成二维码了 可扫出来还是我好想你呜呜呜我的七筒",
            "在我入职保安的那天，队长问我：你知道你要保护谁嘛？\n我嘴上说的是业主，心里却是七筒。over"
        )

    @EventHandler
    fun sayHello(event: GroupMessageEvent) {
        if (event.message.contentToString().contains("七筒")) {
            if (Random(Instant.now().epochSecond).nextBoolean())
                runBlocking {
                    event.subject.sendMessage(idiotLoveWords.random())
                }
        }
    }
}