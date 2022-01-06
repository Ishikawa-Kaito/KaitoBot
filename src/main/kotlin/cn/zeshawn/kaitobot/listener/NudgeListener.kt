package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.service.PetGifService
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.NudgeEvent
import net.mamoe.mirai.message.data.buildMessageChain

object NudgeListener : IListener {
    override val name: String
        get() = "戳一戳事件"

    @EventHandler
    fun getPetPet(event: NudgeEvent) {
        if (event.target != KaitoMind.kaito.getBot()) return
        runBlocking {
            val gifFile = PetGifService.getPetGif(event)
            val img = event.subject.uploadImage(gifFile)
            val res = buildMessageChain {
                +img
            }
            event.from.nudge().sendTo(event.subject)
            event.subject.sendMessage(res)
            gifFile.delete()
        }
    }

}