package cn.zeshawn.kaitobot.service

import cn.hutool.http.HttpRequest
import java.io.InputStream

object TwitterService {
    private const val UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36 Edg/94.0.992.50"

    private const val baseUrl = "https://makeitaquote.com/generate?tweet="
    fun getTweet(url: String): InputStream {
        return HttpRequest.get("${baseUrl}${url}").execute().bodyStream()
    }
}