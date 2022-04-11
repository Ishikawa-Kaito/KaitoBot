package cn.zeshawn.kaitobot.service

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.File
import javax.imageio.ImageIO

internal class WordleServiceTest {

    @Test
    fun getWords() {
        val image = ImageIO.read(File("F:\\wordle_test\\5.png"))
        WordleService.getWords(image)
    }
}