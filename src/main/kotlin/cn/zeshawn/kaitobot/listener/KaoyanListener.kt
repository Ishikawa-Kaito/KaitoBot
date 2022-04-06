package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain
import java.time.Instant
import java.time.LocalDate

object KaoyanListener : IListener {
    override val name: String
        get() = "考研提醒器"

    private val remindList = listOf(807664723L)
    private var lastTalk = mutableMapOf(0L to 0L)
    private val kaoyanDate = LocalDate.parse("2023-12-24")

    @EventHandler
    fun remindYou(event: GroupMessageEvent) {
        val id = event.sender.id
        if (id in remindList) {
            if (lastTalk.containsKey(id)) {
                val last = lastTalk[id]!!
                if (anotherDay(Instant.now().epochSecond, last)) {
                    lastTalk[id] = Instant.now().epochSecond
                    runBlocking {
                        event.subject.sendMessage(
                            buildMessageChain {
                                +At(id)
                                +"这是你今天第一次说话，距离2023年考研还有${LocalDate.now().until(kaoyanDate).days}天"
                            }
                        )
                    }
                }
            }
        }
    }

    fun anotherDay(a: Long, b: Long): Boolean {
        return a / (24 * 60 * 60 * 1000L) - b / (24 * 60 * 60 * 1000L) > 0
    }

}