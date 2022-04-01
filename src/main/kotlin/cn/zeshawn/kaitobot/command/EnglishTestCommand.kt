package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.command.base.ConversationCommand
import cn.zeshawn.kaitobot.data.VocabularyRank
import cn.zeshawn.kaitobot.data.WordData
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.manager.SessionManager
import cn.zeshawn.kaitobot.session.EnglishTestAttender
import cn.zeshawn.kaitobot.session.EnglishTestSession
import cn.zeshawn.kaitobot.session.base.Session
import cn.zeshawn.kaitobot.session.base.SessionTarget
import cn.zeshawn.kaitobot.util.isAlphabet
import cn.zeshawn.kaitobot.util.isNumeric
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.content
import java.time.Duration
import java.time.LocalDateTime

object EnglishTestCommand : ChatCommand, ConversationCommand {
    override val name: String
        get() = "英语测试"
    override val alias: List<String>
        get() = listOf("english", "word", "jdc")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        if (!SessionManager.hasSessionBySubject(event.subject.id, this::class.java)) {
            var rank = (1 until 16).random() - 1
            val word = when {
                args.isEmpty() -> {
                    WordData.getRandomWord(VocabularyRank.values()[rank])
                }
                args.size == 1 -> {
                    if (args[0] in listOf("?", "help")) return getHelp().toChain()
                    else {
                        if (args[0].isNumeric()) {
                            rank = args[0].toInt() - 1
                            if (rank in (0 until 15))
                                WordData.getRandomWord(VocabularyRank.values()[rank])
                            else
                                return getHelp().toChain()
                        } else {
                            return getHelp().toChain()
                        }
                    }
                }
                else -> {
                    return getHelp().toChain()
                }
            }
            val sessionTarget = if (event is GroupMessageEvent)
                SessionTarget(groupId = event.subject.id)
            else
                SessionTarget(privateId = event.subject.id)
            KaitoMind.KaitoLogger.info("[记单词] ($sessionTarget) 生成的题目是 (${word.word}, ${word.tran})")
            SessionManager.insertSession(EnglishTestSession(sessionTarget, word))
            return """
                本单词来自${VocabularyRank.values()[rank].desc}
                ${word.tran}
                ${word.pos}
            """.trimIndent().toChain()
        } else {
            return "上一局还没结束呢。".toChain()
        }
    }

    override fun getHelp(): String {
        val sb = StringBuilder(
            """
            来记单词吧！
            /jdc [单词等级] 单词等级见下
        """.trimIndent()
        )
        VocabularyRank.values().forEach {
            sb.append("\n${it.rank} ${it.desc}")
        }
        return sb.toString()
    }

    override suspend fun handle(event: MessageEvent, session: Session, user: User) {
        val answer = (session as EnglishTestSession).answer
        session.lastAnswerTime = LocalDateTime.now()
        val tryAnswer = event.message.content
        var attender = session.getAttender(user.userId)

        if (tryAnswer.replace("\\s".toRegex(), "").isAlphabet()) {
            if (attender == null) {
                attender = EnglishTestAttender(event.sender.id, event.sender.nameCardOrNick)
                session.users.add(attender)
            }
            attender.answerTimes += 1

            if (tryAnswer.lowercase() == answer.word.lowercase()) {
                session.usedTime = Duration.between(session.createdTime, LocalDateTime.now())
                val sb =
                    StringBuilder("${attender.username} 答对了!\n总用时: ${session.usedTime.seconds}s\n\n")
                val list = session.users.sortedBy { (it as EnglishTestAttender).answerTimes }
                list.forEach {
                    sb.append("\n" + (it as EnglishTestAttender).username).append(" ").append(it.answerTimes)
                        .append("次\n")
                }
                event.subject.sendMessage(sb.toString().toChain())
                SessionManager.removeSession(session)
            } else {
                event.subject.sendMessage("${attender.username}认为是${tryAnswer}\n很遗憾，猜错了。".toChain())
            }
        } else {
            when (tryAnswer) {
                "退出", "放弃", "结束" -> {
                    event.subject.sendMessage("本次测试结束。正确答案是{${answer.word}}")
                    SessionManager.removeSession(session)
                }
                "提示" -> {
                    event.subject.sendMessage("第一个字母是${session.answer.word[0]}")
                    if (session.answer.word.trim().contains(" ")) {
                        event.subject.sendMessage("而且这是一个短语。")
                    }
                }
            }

        }
    }
}