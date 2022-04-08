package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.service.OreoService
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent

object OreoListener : IListener {
    override val name: String
        get() = "奥利奥监听器"

    @EventHandler
    fun generateOreo(event: MessageEvent) {
        val reg = "[奥利[与和]]*".toRegex()
        val msg = event.message.contentToString()
        if (reg.matches(msg)) {
            val imageStream = OreoService.generateOreo(msg) ?: return
            runBlocking {
                val image = event.subject.uploadImage(imageStream = imageStream)
                event.subject.sendMessage(image)
            }

        }

    }
}