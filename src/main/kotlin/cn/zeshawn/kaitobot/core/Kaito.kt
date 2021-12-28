package cn.zeshawn.kaitobot.core

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.KaitoMind.kaito
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.FileCacheStrategy

class Kaito {
    private lateinit var bot:Bot


    suspend fun login(){
        KaitoMind.KaitoLogger.info { "正在尝试登录......" }
        init()
        bot.login()
    }

    suspend fun join() {
        bot.join()
    }

    private fun init(){
        val config = BotConfiguration.Default.apply {
            fileBasedDeviceInfo()
        }
        bot = BotFactory.newBot(qq = KaitoMind.config.botId, password = KaitoMind.config.botPassword, configuration = config)
    }

    fun isInitialized(): Boolean {
        return ::bot.isInitialized && bot.isOnline
    }
}