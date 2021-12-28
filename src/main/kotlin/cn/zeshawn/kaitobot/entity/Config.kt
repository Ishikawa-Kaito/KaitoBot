package cn.zeshawn.kaitobot.entity

data class Config(
    var botId: Long = 0L,
    var botPassword: String = "",
    var isDebugMode: Boolean = false,
    var ownerId: Long = 0L,
    var commandPrefix: MutableList<String> = mutableListOf("/"),
    var NeteaseMusicApiBase: String = "http://119.29.171.164:3000",
    var NeteaseEmoSongList: List<Long> = listOf(5130412130L, 428950382L)
)
