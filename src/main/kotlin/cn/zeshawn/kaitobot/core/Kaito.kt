package cn.zeshawn.kaitobot.core

import cn.zeshawn.kaitobot.KaitoMind
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.utils.BotConfiguration

class Kaito {
    private lateinit var bot: Bot

    suspend fun login() {
        KaitoMind.KaitoLogger.info { "正在尝试登录......" }
        init()
        bot.login()
        KaitoLauncher.intercept(bot)
    }

    suspend fun join() {
        bot.join()
    }

    private fun init() {
        val config = BotConfiguration.Default.apply {
            fileBasedDeviceInfo()
        }
        bot = BotFactory.newBot(
            qq = KaitoMind.config.botId,
            password = KaitoMind.config.botPassword,
            configuration = config
        )
    }

    fun isInitialized(): Boolean {
        return ::bot.isInitialized && bot.isOnline
    }
}