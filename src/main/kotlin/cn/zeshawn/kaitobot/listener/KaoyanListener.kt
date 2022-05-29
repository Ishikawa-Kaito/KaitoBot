package cn.zeshawn.kaitobot.listener

import cn.hutool.core.date.LocalDateTimeUtil
import cn.zeshawn.kaitobot.listener.base.IListener
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.buildMessageChain
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

object KaoyanListener : IListener {
    override val name: String
        get() = "考研提醒器"

    private val remindList = listOf(807664723L)
    private var lastTalk = mutableMapOf(0L to 0L)
    private val kaoyanDate = LocalDate.parse("2022-12-24")


    fun remindYou(event: GroupMessageEvent) {
        val id = event.sender.id
        if (id in remindList) {
            if (!lastTalk.containsKey(id)) {
                lastTalk[id] = 0
            }
            val last = lastTalk[id]!!
            if (anotherDay(Instant.now().epochSecond, last)) {
                lastTalk[id] = Instant.now().epochSecond
                runBlocking {
                    event.subject.sendMessage(
                        buildMessageChain {
                            +At(id)
                            +"\n距离2023年考研还有${
                                LocalDateTimeUtil.between(LocalDateTime.now(), kaoyanDate.atTime(0, 0)).toDays()
                            }天"
                        }
                    )
                }
            }
        }
    }

    private fun anotherDay(a: Long, b: Long): Boolean {
        return a / (24 * 60 * 60 * 1000L) - b / (24 * 60 * 60 * 1000L) > 0
    }

}