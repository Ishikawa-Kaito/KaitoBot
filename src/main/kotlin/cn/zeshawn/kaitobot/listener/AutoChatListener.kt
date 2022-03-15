package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.entity.Group
import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.toMessageChain
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

object AutoChatListener : IListener {
    override val name: String
        get() = "自动聊天"

    @EventHandler
    fun messageArrive(event: GroupMessageEvent) {
        ChatManager.learn(event)
        val reply = ChatManager.getAnswer(event)
        val group = Group.getGroupOrAdd(event.group.id)
        if (group.params["answer"] != true) return
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

    private fun createQuestion(id: Long, message: MessageChain) {
        val q = Question(message, mutableListOf())
        if (chatLibrary[id] == null) { // 如果该群还没有记录
            chatLibrary[id] = mutableListOf()
        }
        if (chatLibrary[id]!!.all { it.message != message }) { // 如果还没有记录该问题
            chatLibrary[id]!!.add(q)
        }
    }

    private fun createAnswer(id: Long, q: MessageChain, a: MessageChain) {
        val answer = Answer(a, Instant.now().epochSecond)
        chatLibrary.getValue(id).find { it.message == q }!!.answers.add(answer)
    }

    fun learn(event: GroupMessageEvent) {
        val id = event.subject.id
        val message = event.message.filter { it !is MessageSource }.toMessageChain()
        if (!messageSequence.containsKey(id)) {
            messageSequence[id] = MessageSequence(EmptyMessageChain, 0, EmptyMessageChain)
        }
        if (Instant.now().epochSecond - messageSequence.getValue(id).time > 900) {
            messageSequence[id] = MessageSequence(message, Instant.now().epochSecond, EmptyMessageChain)
        }
        if (message == messageSequence.getValue(id).message) {
            createQuestion(id, message)
            messageSequence.getValue(id).before = message
        } else {
            createQuestion(id, message)
            createAnswer(id, messageSequence.getValue(id).before, message)
            messageSequence.getValue(id).before = message
        }
    }

    fun getAnswer(event: GroupMessageEvent): MessageChain {
        val id = event.subject.id
        val message = event.message.filter { it !is MessageSource }.toMessageChain()
        val questions = if (chatLibrary.containsKey(id)) {
            chatLibrary.getValue(id)
        } else {
            chatLibrary.values.random()
        }
        val answers = questions.find { it.message == message }?.answers!!
        if (answers.isNotEmpty()) {
            return answers.random().message
        }
        return EmptyMessageChain
    }


}


data class Answer(
    var message: MessageChain,
    var time: Long,

    )

data class Question(
    var message: MessageChain,
    var answers: MutableList<Answer>
)

data class MessageSequence(
    var message: MessageChain,
    var time: Long,
    var before: MessageChain
)

