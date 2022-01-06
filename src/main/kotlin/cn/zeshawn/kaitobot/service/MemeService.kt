package cn.zeshawn.kaitobot.service

import cn.hutool.http.HttpRequest
import cn.zeshawn.kaitobot.KaitoMind
import java.io.InputStream

object MemeService {
    private const val apiBase: String = "https://meme-api.herokuapp.com/gimme"

    private fun getRandomMemeUrl(): String {
        val response = HttpRequest.get(apiBase).setHttpProxy("127.0.0.1", 10809).execute().body()
        val responseString = response ?: return ""

        return try {
            val tree = KaitoMind.mapper.readTree(responseString)
            val memeUrl = tree["url"].asText()
            KaitoMind.KaitoLogger.info("memeApi取得一张memeUrl(${memeUrl})")
            memeUrl
        } catch (e: Exception) {
            KaitoMind.KaitoLogger.error("emoApi调用失败: 回调结果异常 (${e.message})", e.cause)
            ""
        }
    }

    fun getRandomMeme(): InputStream? {
        val url = getRandomMemeUrl()
        if (url.isNotEmpty()) {
            return HttpRequest.get(url).setHttpProxy("127.0.0.1", 10809).execute().bodyStream()
        }
        return null
    }


}