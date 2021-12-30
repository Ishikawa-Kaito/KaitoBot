package cn.zeshawn.kaitobot.service

import cn.hutool.http.HttpRequest
import cn.hutool.http.HttpUtil
import cn.zeshawn.kaitobot.KaitoMind
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.sql.Time
import java.util.*


object FurryPicService {
    private const val apiBase: String = "http://127.0.0.1:3001"

    private fun getFurryPic(nsfw:Int, keyword:String = ""): InputStream? {

        val response = if (keyword.isEmpty()){
            HttpUtil.get("${apiBase}/random?nsfw=${nsfw}")
        }else{
            HttpUtil.get("${apiBase}/search?keyword=${keyword}&nsfw=${nsfw}&page=${Random().nextInt(5)}")
        }
        val responseString = response ?: return null
        val tree = KaitoMind.mapper.readTree(responseString)
        if (tree.isNull || tree.isEmpty) {
            return null
        }
        val randomChoice = Random().nextInt(tree.size())
        val url = tree[randomChoice]["thumb"]["large"].asText()
        return ByteArrayInputStream(HttpUtil.downloadBytes(url))
    }

    fun searchPic(nsfw:Int,keyword: String):InputStream?{
        return getFurryPic(nsfw, keyword)
    }

    fun randomPic(nsfw: Int):InputStream?{
        return getFurryPic(nsfw)
    }
}