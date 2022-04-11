package cn.zeshawn.kaitobot.service

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.io.File
import javax.imageio.ImageIO

internal class WordleServiceTest {

    @Test
    fun getWords() {
        val image = ImageIO.read(File("C:\\Users\\63086\\Desktop\\wordle\\5.png"))
        WordleService.solve(image)
    }
}