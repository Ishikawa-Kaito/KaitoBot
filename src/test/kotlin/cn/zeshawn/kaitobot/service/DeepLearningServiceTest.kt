package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.util.Save
import org.junit.jupiter.api.Test

import java.io.File
import javax.imageio.ImageIO

internal class DeepLearningServiceTest {

    @Test
    fun detectHaoHao() {
    }

    @Test
    fun fitYoloSize() {
    val image = ImageIO.read(File("C:\\Users\\63086\\Documents\\HiroshiFans\\raw_data\\images\\14.jpg"))
    val b = DeepLearningService.detectHaoHao(image)
        DeepLearningService.drawRect(image,b).Save("test.png")
    }
}