package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.Bible
import cn.zeshawn.kaitobot.data.BibleData
import cn.zeshawn.kaitobot.util.getUrlStream
import cn.zeshawn.kaitobot.util.hash
import cn.zeshawn.kaitobot.util.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import java.io.File
import javax.imageio.ImageIO

object BibleService {

    private val savePath = KaitoMind.root.absolutePath + "/bibles"

    fun getRandomBible(event: GroupMessageEvent) {
        val bible = BibleData.getRandomBibleByGroup(event.subject.id)
        val image = ImageIO.read(File(bible.imagePath))
        val stream = image.toInputStream()
        runBlocking {
            event.subject.sendImage(stream)
        }
    }

    suspend fun uploadBible(event: GroupMessageEvent): Boolean {
        val image = event.message.filterIsInstance<Image>()
        val count = image.sumOf {
            if (uploadBible(it, event.sender.id, event.subject.id)) 1 else 0L
        }
        if (count > 0) {
            event.subject.sendMessage("成功上传${count}条圣经")
            return true
        }
        return false
    }

    suspend fun uploadBible(image: Image, uploaderId: Long, groupId: Long): Boolean {
        val stream = image.queryUrl().getUrlStream()
        val bibleImage = ImageIO.read(stream)
        val imageHash = bibleImage.hash()
        if (!BibleData.isBibleExisted(imageHash)) { // 不存在这张图
            val path = "${savePath}/${imageHash}.png"
            val bible = Bible(0, groupId, uploaderId, imageHash, path)
            if (BibleData.addNewBible(bible)) {
                withContext(Dispatchers.IO) {
                    ImageIO.write(bibleImage, "png", File(ensureDir(path)))
                }
                return true
            }
        }
        return false
    }


    fun ensureDir(path: String): String {
        val file = File(path)
        if (!file.exists()) {
            file.mkdirs()
        }
        return path
    }


}