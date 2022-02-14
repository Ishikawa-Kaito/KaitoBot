package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent
import net.mamoe.mirai.event.events.FriendMessageEvent

object GroupInviteListener : IListener {
    override val name: String
        get() = "通过群申请监听器"


    @EventHandler
    fun groupInviteEvent(event: BotInvitedJoinGroupRequestEvent) {
        val groupId = event.groupId
        val invitorId = event.invitorId
        val requestMsg = "${invitorId}邀请机器人加入${groupId}，是否同意"
        runBlocking {
            event.bot.getFriend(KaitoMind.config.ownerId)!!.sendMessage(requestMsg)
            event.bot.eventChannel.filter {
                it is FriendMessageEvent &&
                        it.sender.id == KaitoMind.config.ownerId
            }.subscribeOnce<FriendMessageEvent> {
                if (message.contentToString().contains("ok")) {
                    event.accept()
                } else if (message.contentToString().contains("no")) {
                    event.ignore()
                }
            }
        }
    }


}