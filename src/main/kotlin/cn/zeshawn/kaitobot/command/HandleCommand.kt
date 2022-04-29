package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.command.base.ConversationCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.manager.SessionManager
import cn.zeshawn.kaitobot.service.HandleGame
import cn.zeshawn.kaitobot.service.HandleService
import cn.zeshawn.kaitobot.session.HandleSession
import cn.zeshawn.kaitobot.session.base.Session
import cn.zeshawn.kaitobot.session.base.SessionTarget
import cn.zeshawn.kaitobot.util.isAlphabet
import cn.zeshawn.kaitobot.util.toChain
import cn.zeshawn.kaitobot.util.toInputStream
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.message.data.content

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
            SessionManager.insertSession(HandleSession(sessionTarget, hg))
            KaitoMind.KaitoLogger.info("[汉兜] ($sessionTarget) 生成的题目是 (${hg.answer.word})")
            val img = event.subject.uploadImage(hg.draw().toInputStream())
            return buildMessageChain {
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
        if (tryAnswer.any { it.toString().isAlphabet() || it.isDigit() }) return
        if (tryAnswer.length != 4) return
        if (HandleService.isIdiom(tryAnswer)) {
            hg.attempt(tryAnswer)
            event.subject.sendImage(hg.draw().toInputStream())
        }
    }
}