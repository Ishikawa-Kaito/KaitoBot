package cn.zeshawn.kaitobot.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EmojiMixServiceKtTest {

    @Test
    fun getEmojiUnicode() {
        val testEmoji = "😶‍🌫️"
        assertEquals(testEmoji.getEmojiUnicode(), "u1f636-u200d-u1f32b-ufe0f")
        assertEquals("test".getEmojiUnicode(), "")
    }
}