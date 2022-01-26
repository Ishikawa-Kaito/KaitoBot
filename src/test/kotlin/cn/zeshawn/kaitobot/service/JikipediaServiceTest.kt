package cn.zeshawn.kaitobot.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class JikipediaServiceTest {

    @Test
    fun getId() {
        val id = JikipediaService.getId("田鼠哥")
        println(id)
        assertEquals(id, 1040642445)
    }

    @Test
    fun getDefinition() {
        val id = JikipediaService.getId("田鼠哥")
        println(id)
        assertEquals(id, 1040642445)
        val res = JikipediaService.getDefinition(id)
        assertNotEquals(res.second.size,0)
    }
}