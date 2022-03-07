package cn.zeshawn.kaitobot.service

import cn.hutool.core.io.FileUtil
import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpUtil
import cn.zeshawn.kaitobot.KaitoMind
import okio.ByteString.Companion.encode
import java.io.File
import java.nio.charset.Charset

object EmojiMixService {
    private const val apiBase = "https://www.gstatic.com/android/keyboard/emojikitchen"
    private val dateList = listOf("20201001", "20210218", "20210521", "20210831", "20211115")
    private val localCacheDir: String by lazy { "${KaitoMind.root}/emojiMix" }

    private fun checkEmojiMix(e1: String, e2: String): Boolean {
        if (checkLocalCache(e1, e2)) {
            return true
        } else if (checkServerExistence(e1, e2)) {
            return true
        }
        return false
    }

    private fun checkLocalCache(e1: String, e2: String): Boolean {
        val filename = "${e1.getEmojiUnicode()}_${e2.getEmojiUnicode()}.png"
        val file = File("${localCacheDir}/${filename}")
        return file.exists()
    }

    private fun checkServerExistence(e1: String, e2: String): Boolean {
        val filename = "${e1.getEmojiUnicode()}_${e2.getEmojiUnicode()}.png"
        dateList.forEach { date ->
            val url = "${apiBase}/${date}/${e1.getEmojiUnicode()}/${filename}"
            val res = HttpRequest.get(url).execute()
            if (res.isOk) {
                HttpUtil.downloadFile(url, FileUtil.file("${localCacheDir}/${filename}"))
                return true
            }
        }
        return false
    }

    fun getEmojiMix(e1: String, e2: String): File? {
        if (checkEmojiMix(e1, e2)) {
            return File("${localCacheDir}/${e1.getEmojiUnicode()}_${e2.getEmojiUnicode()}.png")
        } else if (checkEmojiMix(e2, e1)) {
            return File("${localCacheDir}/${e2.getEmojiUnicode()}_${e1.getEmojiUnicode()}.png")
        }
        return null
    }


}


fun String.getEmojiUnicode(): String {
    // emoji表情的格式不固定，可能出现组合表情，控制符占两个字节，表情符占四个字节
    // Char为定长2字节
    return if (this.length > 2) {
        // 找到第一个控制字符,
        var i = this.indexOfFirst { !it.isEmoji() }
        if (i == -1) {
            // 超过四个字节，但是不存在控制字符，这是不可能的，可以认定是非法数据，直接退出
            return ""
        }
        if (i == 0) i++
        // 此时带控制字符，结果要用"-"分割
        "${this.substring(0, i).getEmojiUnicode()}-${this.substring(i).getEmojiUnicode()}"
    } else {
        // 不带控制字符直接输出utf32编码，去除前置的0
        "u${this.encode(Charset.forName("UTF-32")).hex().replaceFirst("^0*".toRegex(), "")}"
    }
}

fun Char.isEmoji(): Boolean {
    return !(this.code == 0x0 ||
            this.code == 0x9 ||
            this.code == 0xA ||
            this.code == 0xD ||
            this.code in 0x20..0xD7FF ||
            this.code in 0xE000..0xFFFD ||
            this.code in 0x100000..0x10FFFF)
}

fun String.isEmoji(): Boolean = this.all { it.isEmoji() }

