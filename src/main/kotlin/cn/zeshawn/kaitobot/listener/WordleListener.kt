package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.entity.Group
import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.service.WordleService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image

object WordleListener : IListener {
    override val name: String
        get() = "自动玩wordle"


    @EventHandler
    fun solveWordle(event: GroupMessageEvent) {
        val group = Group.getGroupOrAdd(event.subject.id)
        if (group.params["auto_wordle"] != true) return
        if (!event.message.contains(Image)) return
        runBlocking {
            val msg = WordleService.solve(event.message)
            if (msg.isNotBlank()) {
                delay(1000)
                event.subject.sendMessage(msg)
            }
        }
    }
}