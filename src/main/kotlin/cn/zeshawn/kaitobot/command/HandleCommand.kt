package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.command.base.ConversationCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.manager.SessionManager
import cn.zeshawn.kaitobot.service.BankService
import cn.zeshawn.kaitobot.service.HandleGame
import cn.zeshawn.kaitobot.session.HandleSession
import cn.zeshawn.kaitobot.session.HandleSessionAttender
import cn.zeshawn.kaitobot.session.base.Session
import cn.zeshawn.kaitobot.session.base.SessionTarget
import cn.zeshawn.kaitobot.util.isChinese
import cn.zeshawn.kaitobot.util.toChain
import cn.zeshawn.kaitobot.util.toInputStream
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.*

object HandleCommand : ChatCommand, ConversationCommand {
    override val name: String
        get() = "汉兜游戏"
    override val alias: List<String>
        get() = listOf("handle", "hd", "handou")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        if (!SessionManager.hasSessionBySubject(event.subject.id, this::class.java)) {
            val sessionTarget = if (event is GroupMessageEvent)
                SessionTarget(groupId = event.subject.id)
            else
                SessionTarget(privateId = event.subject.id)
            val hg = HandleGame()
            SessionManager.insertSession(HandleSession(sessionTarget, hg, 120))
            KaitoMind.KaitoLogger.info("[汉兜] ($sessionTarget) 生成的题目是 (${hg.answer.word})")
            val img = event.subject.uploadImage(hg.draw().toInputStream())
            return buildMessageChain {
                +PlainText("在六次尝试中猜出一个成语，成语可能由下列字组成。黄色代表该字（或声母韵母声调）在答案中存在，不过不在这个位置。灰色代表不存在，绿色代表正确。")
                +img
                +PlainText(hg.candidateLetters.joinToString(", ").trimEnd(',', ' '))
            }
        } else {
            return "上一局还没结束捏。".toChain()
        }
    }

    override fun getHelp() =
        "Handle~汉兜，在 6 次尝试中猜出成语。"

    override suspend fun handle(event: MessageEvent, session: Session, user: User) {
        val hg = (session as HandleSession).hg
        val tryAnswer = event.message.content
        var attender = session.getAttender(user.userId)
        if (attender == null) {
            attender = HandleSessionAttender(event.sender.id, event.sender.nameCardOrNick)
            session.users.add(attender)
        }
        if (tryAnswer.contains("提示")) {
            event.subject.sendMessage(hg.tips.value)
        }
        if (!tryAnswer.isChinese()) return
        if (tryAnswer.length != 4) return
        if (HandleGame.isIdiom(tryAnswer)) {
//            不在候选也可以答，降低难度
//            if (tryAnswer.any{ it.toString() !in hg.candidateLetters})
//                return
            if (!hg.isAnswerValid(tryAnswer)) {
                event.subject.sendMessage("已经猜过这个答案了捏。")
                return
            }
            if (hg.attempt(tryAnswer)) {
                SessionManager.removeSession(session)
                val bonus = (7 - hg.triedTimes) * 100L
                BankService.addDCoin(event.sender.id, bonus)
                event.subject.sendMessage(buildMessageChain {
                    +At(event.sender)
                    +PlainText(" 答对了！获得了 $bonus 斗币")
                })
                event.subject.sendImage(hg.generateResult().toInputStream())
            } else {
                if (hg.isOver) {
                    session.deprecated()
                    SessionManager.removeSession(session)
                } else event.subject.sendImage(hg.draw().toInputStream())
            }
        } else {
            event.subject.sendMessage("${tryAnswer}不是个成语捏。")
        }

    }
}