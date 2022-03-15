package cn.zeshawn.kaitobot.service

import org.jsoup.Jsoup

object TwitterService {
    private const val UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36 Edg/94.0.992.50"


    fun getTweet(url: String) {
        val conn = Jsoup.connect(url).proxy("127.0.0.1", 10809)
        conn.userAgent(UA)
        val response = conn.execute().parse()
        return
    }
}