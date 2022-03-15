package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.PlainText

object TwitterListener : IListener {
    override val name: String
        get() = "推特链接监听器"


    @EventHandler
    fun getTwitterLink(event: GroupMessageEvent) {
        event.message.forEach {
            when (it) {
                is PlainText -> {
                    if (mathTwitterLink(it.content)) {
                        val tweetId = it.content.split("/").last()
                    }
                }
            }
        }
    }


    private fun mathTwitterLink(link: String): Boolean {
        val regx = "^.*?\\btwitter\\.com/\\w*/\\w*/\\d*".toRegex()
        return regx.matches(link)
    }
}