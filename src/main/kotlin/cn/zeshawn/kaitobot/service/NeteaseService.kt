package cn.zeshawn.kaitobot.service

import cn.hutool.http.HttpUtil
import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.KaitoMind.KaitoLogger
import cn.zeshawn.kaitobot.KaitoMind.mapper
import cn.zeshawn.kaitobot.util.toChain
import com.fasterxml.jackson.databind.JsonNode
import net.mamoe.mirai.message.data.MessageChain
import java.time.Instant
import kotlin.random.Random

object NeteaseService {
    private fun getSong(name: String): Long {
        val response =
            HttpUtil.get("${KaitoMind.config.NeteaseMusicApiBase}/cloudsearch?type=1&limit=1&keywords=${name}")
        val responseString = response ?: return 0
        val tree = mapper.readTree(responseString)
        if (tree.isNull || tree.isEmpty || tree["result"]["songCount"].asInt() == 0) {
            return 0
        }
        try {
            KaitoLogger.info("emoApi取得搜索关键词${name}的id是${tree["result"]["songs"][0]["id"].asLong()}")
            return tree["result"]["songs"][0]["id"].asLong()
        } catch (e: Exception) {
            KaitoLogger.error("emoApi调用失败: 回调结果异常 (${e.message})", e.cause)
        }
        return 0
    }

    private fun getComments(songId: Long, nums: Int): List<String> {

        val response =
            HttpUtil.get("${KaitoMind.config.NeteaseMusicApiBase}/comment/new?id=${songId}&type=0&pageSize=${nums}&sortType=1")
        val responseString = response ?: return listOf()
        val tree = mapper.readTree(responseString)

        if (tree.isNull || tree.isEmpty || tree["code"].asInt() != 200) {
            return listOf()
        }

        if (tree["data"]["totalCount"].asInt() < 5) {
            return listOf()
        }
        val resList = mutableListOf<String>()
        val commentNums = tree["data"]["comments"].size()
        for (i in 0 until commentNums) {
            resList.add(tree["data"]["comments"][i]["content"].toString())
        }
        return resList
    }

    private fun getSongList(id: Long): JsonNode {
        val response = HttpUtil.get("${KaitoMind.config.NeteaseMusicApiBase}/playlist/track/all?id=${id}")
        val responseString = response ?: return mapper.nullNode()
        val tree = mapper.readTree(responseString)
        return tree["songs"]
    }

    private fun getRandomComments(): List<String> {
        val songs = getSongList(KaitoMind.config.NeteaseEmoSongList.random())
        val randomSongIndex = (0..songs.size()).random()
        val songId = songs[randomSongIndex]["id"].asLong()
        KaitoLogger.info("emoApi随机emo挑选的id是${songId}")
        return getComments(songId, 5)
    }

    fun getOne(keyword: String): MessageChain {
        val id = getSong(keyword)
        val comments = getComments(id, 20)
        return chainWrapper(comments)
    }

    fun getRandomOne(): MessageChain {
        return chainWrapper(getRandomComments())
    }

    fun getBackupOne(): MessageChain {
        return chainWrapper(KaitoMind.music163Comments)
    }


    private fun chainWrapper(comments: List<String>): MessageChain {
        if (comments.isNotEmpty()) {
            val random = Random(Instant.now().epochSecond)
            val choice = random.nextInt(comments.size)
            val comment = comments[choice].replace(Regex("\""), "").replace("\\n", "\n")
            return comment.toChain()
        }
        throw IllegalArgumentException()
    }
}