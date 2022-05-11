package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.service.BlackjackService
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent

object BlackJackListener : IListener {
    override val name: String
        get() = "二十一点"


    @EventHandler
    fun blackjack(event: GroupMessageEvent) {
        runBlocking {
            BlackjackService.handle(event)
        }
    }
}