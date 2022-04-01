package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.service.TwitterService
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toMessageChain

object TwitterListener : IListener {
    override val name: String
        get() = "推特链接监听器"


    @EventHandler
    fun getTwitterLink(event: GroupMessageEvent) {
        event.message.forEach {
            when (it) {
                is PlainText -> {
                    if (mathTwitterLink(it.content)) {
                        runBlocking {
                            val img = event.subject.uploadImage(TwitterService.getTweet(it.content))
                            event.subject.sendMessage(img.toMessageChain())
                        }

                    }
                }
            }
        }
    }


    private fun mathTwitterLink(link: String): Boolean {
        val regx = "^.*?\\btwitter\\.com/\\w*/\\w*/\\d*\\S*".toRegex()
        return regx.matches(link)
    }
}