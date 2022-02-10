package cn.zeshawn.kaitobot.service

import cn.hutool.http.HttpUtil
import io.ktor.http.*
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.io.ByteArrayInputStream

object JikipediaService {
    private const val searchUrl: String = "https://jikipedia.com/search?phrase="
    private const val definitionUrl: String = "https://jikipedia.com/definition/"
    private const val UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.81 Safari/537.36 Edg/94.0.992.50"


    fun getId(keyword: String): Int {
        val conn = Jsoup.connect("${searchUrl}${keyword}")
        conn.userAgent(UA)
        val response = conn.execute()
        if (response.statusCode() != HttpStatusCode.OK.value)
            return -1
        val doc = response.parse()
        if (doc.title().contains("moss"))
            return -1

        val tile = doc.selectFirst("#search > div > div.masonry")
            ?.getElementsByClass("tile")?.get(0)
        return tile?.attr("data-id")?.toInt() ?: 0
    }

    fun getDefinition(id: Int): Pair<String, List<ByteArrayInputStream>> {
        val conn = Jsoup.connect("${definitionUrl}${id}")
        conn.userAgent(UA)
        val response = conn.execute()
        if (response.statusCode() != HttpStatusCode.OK.value)
            return Pair("", listOf())
        val doc = response.parse()
        if (doc.title().contains("moss"))
            return Pair("", listOf())

        val title = doc.getElementsByClass("title")[0]?.text() ?: "获取失败"
        val render = doc.getElementsByClass("brax-render")
        val content = render.first()?.allElements?.let { concatContent(it) }
        val result = "${title}:\n${content}"
        val cardMiddle = doc.getElementsByClass("card-middle")
        val images = cardMiddle.first()?.getElementsByClass("show-images-img")
        val imageList = images?.let { getImages(it) }
        return Pair(result, imageList ?: listOf())
    }

    private fun concatContent(elements: Elements): String {
        return if (elements.isNotEmpty()) {
            buildString {
                elements.forEach { ele ->
                    if (ele.className().contains("text") || ele.className() == "highlight" || ele.className()
                            .contains("link")
                    ) {
                        append(ele.text())
                    }
                }
            }
        } else {
            ""
        }
    }

    private fun getImages(elements: Elements): List<ByteArrayInputStream> {
        return if (elements.isNotEmpty()) {
            buildList {
                elements.forEach {
                    if (it.tag().name == "img") {
                        val url = it.attr("src")
                        this.add(ByteArrayInputStream(HttpUtil.downloadBytes(url)))
                    }
                }
            }
        } else {
            listOf()
        }
    }
}