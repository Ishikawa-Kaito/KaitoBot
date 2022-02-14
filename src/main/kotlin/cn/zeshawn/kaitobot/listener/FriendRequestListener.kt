package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.NewFriendRequestEvent

object FriendRequestListener : IListener {
    override val name: String
        get() = "好友请求监听器"


    @EventHandler
    fun friendRequest(event: NewFriendRequestEvent) {
        val requestId = event.fromId
        val requestMsg = "${requestId}请求加机器人好友，是否同意"
        runBlocking {
            event.bot.getFriend(KaitoMind.config.ownerId)!!.sendMessage(requestMsg)
            event.bot.eventChannel.filter {
                it is FriendMessageEvent &&
                        it.sender.id == KaitoMind.config.ownerId
            }.subscribeOnce<FriendMessageEvent> {
                if (message.contentToString().contains("ok")) {
                    event.accept()
                } else if (message.contentToString().contains("no")) {
                    event.reject(false)
                }
            }
        }
    }
}