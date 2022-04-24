package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.service.BibleService
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image

object BibleListener : IListener {
    override val name: String
        get() = "圣经"


    @EventHandler
    fun uploadBible(event: GroupMessageEvent) {
        val message = event.message
        val messageStr = event.message.contentToString()
        if (messageStr.startsWith("上传圣经")) {
            val image = message.filterIsInstance<Image>()
            if (image.isNotEmpty()) {
                runBlocking {
                    BibleService.uploadBible(event)
                }
            }
        } else if (messageStr.startsWith("随机圣经")) {
            BibleService.getRandomBible(event)
        }


    }
}