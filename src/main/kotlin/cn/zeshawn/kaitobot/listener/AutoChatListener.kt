package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.entity.Group
import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.manager.CommandManager
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageChain.Companion.deserializeJsonToMessageChain
import net.mamoe.mirai.message.data.MessageChain.Companion.serializeToJsonString
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

object AutoChatListener : IListener {
    override val name: String
        get() = "自动聊天"

    @EventHandler
    fun messageArrive(event: GroupMessageEvent) {
        val message = event.message.contentToString()
        if (CommandManager.getCommandPrefix(message) != "" && CommandManager.getCommand(message) != null)
        // 带命令的不学习
            return
        ChatManager.learn(event)
        val reply = ChatManager.getAnswer(event)
        val group = Group.getGroupOrAdd(event.group.id)
        if (group.params["answer"] != true) return

        val letTheGodDecide = (0..10).random() > 7

        if (letTheGodDecide) return

        if (reply != EmptyMessageChain) {
            runBlocking {
                event.subject.sendMessage(reply)
            }
        }
    }


}

object ChatManager {

    var chatLibrary: ConcurrentHashMap<Long, MutableList<Question>> = ConcurrentHashMap()

    private var messageSequence: ConcurrentHashMap<Long, MessageSequence> = ConcurrentHashMap()

    private fun createQuestion(id: Long, messageChain: MessageChain) {
        val message = messageChain.serializeToJsonString()
        val q = Question(message, mutableListOf())
        if (chatLibrary[id] == null) { // 如果该群还没有记录
            chatLibrary[id] = mutableListOf()
        }
        if (chatLibrary[id]!!.all { it.message != message }) { // 如果还没有记录该问题
            chatLibrary[id]!!.add(q)
        }
    }

    private fun createAnswer(id: Long, qm: String, am: String) {
        val answer = Answer(am, Instant.now().epochSecond)
        chatLibrary.getValue(id).find { it.message == qm }!!.answers.add(answer)
    }

    fun learn(event: GroupMessageEvent) {
        val id = event.subject.id
        val messageChain = event.message.filterForChat()
        val message = messageChain.serializeToJsonString()
        if (message.isEmpty())
            return
        if (!messageSequence.containsKey(id)) {
            messageSequence[id] =
                MessageSequence(EmptyMessageChain.serializeToJsonString(), 0, EmptyMessageChain.serializeToJsonString())
        }
        if (Instant.now().epochSecond - messageSequence.getValue(id).time > 120) {
            messageSequence[id] =
                MessageSequence(message, Instant.now().epochSecond, EmptyMessageChain.serializeToJsonString())
        }
        if (message == messageSequence.getValue(id).message) {
            createQuestion(id, messageChain)
            messageSequence.getValue(id).before = message
        } else {
            createQuestion(id, messageChain)
            createAnswer(id, messageSequence.getValue(id).before, message)
            messageSequence.getValue(id).before = message
        }
    }

    fun getAnswer(event: GroupMessageEvent): MessageChain {
        val id = event.subject.id
        val message = event.message.filterForChat().serializeToJsonString()
        val questions = if (chatLibrary.containsKey(id)) {
            chatLibrary.getValue(id)
        } else {
            chatLibrary.values.random()
        }
        val answers = if (questions.any { it.message == message }) {
            questions.find { it.message == message }!!.answers
        } else {
            chatLibrary.values.find { questionList -> questionList.any { it.message == message } }
        }
        if (answers != null) {
            if (answers.isNotEmpty()) {
                // return answers.random().message 数据库中存在一些未脱敏内容
                return (answers.random() as Answer).message.deserializeJsonToMessageChain()
            }
        }
        return EmptyMessageChain
    }


    private fun MessageChain.filterForChat(): MessageChain {
        return this.filter {
            if (it is Image) { // 图片只记录表情，不记录照片
                return@filter it.isEmoji
            }
            return@filter when (it) {
                is At, AtAll -> false
                is MessageSource -> false
                is FlashImage -> false
                is QuoteReply -> false
                is Audio -> false
                is FileMessage -> false
                else -> true
            }
        }.toMessageChain()
    }

}


data class Answer(
    var message: String,
    var time: Long,

    )

data class Question(
    var message: String,
    var answers: MutableList<Answer>
)

data class MessageSequence(
    var message: String,
    var time: Long,
    var before: String
)

