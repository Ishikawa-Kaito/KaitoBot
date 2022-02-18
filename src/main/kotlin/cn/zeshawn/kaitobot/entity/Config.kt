package cn.zeshawn.kaitobot.entity

data class Config(
    var botId: Long = 0L,
    var botPassword: String = "",
    var isDebugMode: Boolean = false,
    var ownerId: Long = 0L,
    var commandPrefix: MutableList<String> = mutableListOf("/"),
    var NeteaseMusicApiBase: String = "",
    var NeteaseEmoSongList: List<Long> = listOf()
)
