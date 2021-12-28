package cn.zeshawn.kaitobot.entity

data class Config(
    val botId:Long = 0L,
    val botPassword:String="",
    val isDebugMode:Boolean=false,
    val ownerId:Long = 0L,
    val commandPrefix:MutableList<String> = mutableListOf("/")
)
