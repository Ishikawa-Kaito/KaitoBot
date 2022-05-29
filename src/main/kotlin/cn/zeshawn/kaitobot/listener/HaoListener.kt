package cn.zeshawn.kaitobot.listener

import cn.hutool.http.HttpUtil
import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.service.DeepLearningService
import cn.zeshawn.kaitobot.util.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.firstIsInstanceOrNull
import javax.imageio.ImageIO

object HaoListener : IListener {
    override val name: String
        get() = "浩浩发掘器"


    @EventHandler
    fun detectHao(event: GroupMessageEvent) {
        val img = event.message.firstIsInstanceOrNull<Image>() ?: return
        runBlocking {
            val respondImage = withContext(Dispatchers.IO) {
                ImageIO.read(HttpUtil.downloadBytes(img.queryUrl()).inputStream())
            }
            val boxes = DeepLearningService.detectHaoHao(respondImage)
            if (boxes.isEmpty()) return@runBlocking
            val result = DeepLearningService.drawRect(respondImage, boxes)
            event.subject.sendMessage("米子给打！叔叔找到你啦！")
            event.subject.sendImage(result.toInputStream())
        }
    }
}