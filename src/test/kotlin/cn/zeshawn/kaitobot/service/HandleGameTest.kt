package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.data.IdiomData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class HandleGameTest {

    @BeforeEach
    fun init() {
        IdiomData.load()
    }

    @Test
    fun draw() {
        val hg = HandleGame()
        val tryAnswers = listOf("一往无前", "层峦叠嶂", "危言耸听", "一毛不拔", "年年有余")
        for (a in tryAnswers) {
            if (hg.attempt(a)) {
                break
            } else {
                hg.draw()
            }
        }
    }
}