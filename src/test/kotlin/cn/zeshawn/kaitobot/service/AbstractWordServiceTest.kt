package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.data.AbstractWordData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AbstractWordServiceTest {


    val testString = "大写锁定"

    @BeforeEach
    fun init() {
        AbstractWordData.init()
    }

    @Test
    fun getPinyin() {
        val pinyin = "daxiesuoding"
        assertEquals(pinyin, AbstractWordService.getPinyin(testString))
    }

    @Test
    fun getAbstract() {
        val abstractWord = AbstractWordService.getAbstract(testString)
        assertNotNull(abstractWord)

    }
}