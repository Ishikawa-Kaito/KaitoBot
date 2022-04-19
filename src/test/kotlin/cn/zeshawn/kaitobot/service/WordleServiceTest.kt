package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.data.WordData
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO

internal class WordleServiceTest {

    @Test
    fun getWords() {
        WordData.load()
        val words = WordData.getWordsByLength(10)
        words.forEach {
            println(it)
        }
    }

    @Test
    fun getRows(){
        val img = ImageIO.read(File("F:\\Project\\pythonProject2\\raw_data\\1.png"))
        WordleService.getRows(img)
    }

}