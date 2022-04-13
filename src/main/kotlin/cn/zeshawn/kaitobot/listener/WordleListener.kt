package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.service.WordleService
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image

object WordleListener:IListener {
    override val name: String
        get() = "自动玩wordle"




    @EventHandler
    fun solveWordle(event: GroupMessageEvent){
        if (event.sender.id == 3270864281L) return
        if (!event.message.contains(Image)) return
        runBlocking {
            val msg = WordleService.solve(event.message,event.subject.id)
            if (msg.isNotBlank()){
                event.subject.sendMessage(msg)
            }
        }
    }
}