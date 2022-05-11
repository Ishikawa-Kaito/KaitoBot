package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.command.base.ConversationCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.manager.SessionManager
import cn.zeshawn.kaitobot.service.BankService
import cn.zeshawn.kaitobot.service.WordleGame
import cn.zeshawn.kaitobot.session.WordleSession
import cn.zeshawn.kaitobot.session.WordleSessionAttender
import cn.zeshawn.kaitobot.session.base.Session
import cn.zeshawn.kaitobot.session.base.SessionTarget
import cn.zeshawn.kaitobot.util.isAlphabet
import cn.zeshawn.kaitobot.util.toChain
import cn.zeshawn.kaitobot.util.toInputStream
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*

object WordleCommand : ChatCommand, ConversationCommand {
    override val name: String
        get() = "Wordle Game"
    override val alias: List<String>
        get() = listOf("wordle")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        if (!SessionManager.hasSessionBySubject(event.subject.id, this::class.java)) {
            val sessionTarget = if (event is GroupMessageEvent)
                SessionTarget(groupId = event.subject.id)
            else
                SessionTarget(privateId = event.subject.id)
            val wg = WordleGame()
            SessionManager.insertSession(WordleSession(sessionTarget, wg, 120))
            KaitoMind.KaitoLogger.info("[Wordle] ($sessionTarget) 生成的题目是 (${wg.answer})")
            val img = event.subject.uploadImage(wg.draw().toInputStream())
            return buildMessageChain {
                +PlainText("Wordle game !You need to guess the 5-letter-word in 6 tries. Just type any word. If the letter is guessed correctly and is in the correct place, it will be highlighted in green, if the letter is in the word, but in the wrong place - in yellow, and if the letter is not in the word, it will remain gray. ")
                +img
            }
        } else {
            return "Last round isn't over yet.。".toChain()
        }
    }

    override fun getHelp(): String {
        return "wordle 游戏，在六次尝试中猜单词"
    }

    override suspend fun handle(event: MessageEvent, session: Session, user: User) {
        val wg = (session as WordleSession).wg
        val tryAnswer = event.message.content.uppercase()
        var attender = session.getAttender(user.userId)
        if (attender == null) {
            attender = WordleSessionAttender(event.sender.id, event.sender.nameCardOrNick)
            session.users.add(attender)
        }
        if (!tryAnswer.isAlphabet()) return
        if (tryAnswer.length != 5) return
        if (!wg.isAnswerUsed(tryAnswer)) {
            event.subject.sendMessage("This word has already been guessed.")
            return
        }
        if (!wg.isAnswerValid(tryAnswer)) {
            event.subject.sendMessage("Are you sure $tryAnswer is a valid word?")
            return
        }
        if (wg.attempt(tryAnswer)) {
            SessionManager.removeSession(session)
            val bonus = (7 - wg.triedTimes) * 100L
            BankService.addDCoin(event.sender.id, bonus)
            event.subject.sendMessage(buildMessageChain {
                +At(event.sender)
                +PlainText(" got it！Earned $bonus DCoin!")
            })
            event.subject.sendImage(wg.draw().toInputStream())
        } else {
            if (wg.isOver) {
                session.deprecated()
                SessionManager.removeSession(session)
            } else event.subject.sendImage(wg.draw().toInputStream())
        }

    }
}