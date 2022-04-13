package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.data.WordData
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
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

}