package cn.zeshawn.kaitobot.core

import ch.qos.logback.classic.Level
import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.manager.CommandManager
import cn.zeshawn.kaitobot.manager.DataManager
import cn.zeshawn.kaitobot.manager.MessageManager
import net.mamoe.mirai.Bot

//启动前的准备活动
object KaitoLauncher {
    fun launch() {
        KaitoMind.KaitoLogger.info {
            """

 __  ___      ___       __  .___________.  ______   
|  |/  /     /   \     |  | |           | /  __  \  
|  '  /     /  ^  \    |  | `---|  |----`|  |  |  | 
|    <     /  /_\  \   |  |     |  |     |  |  |  | 
|  .  \   /  _____  \  |  |     |  |     |  `--'  | 
|__|\__\ /__/     \__\ |__|     |__|      \______/  
                                                    
    
            """.trimIndent()
        }
        // 加载数据文件
        DataManager.setup()
        Runtime.getRuntime().addShutdownHook(Thread {
            shutdownTask()
        })

        // 载入全部命令
        CommandManager.autoSetup("cn.zeshawn.kaitobot.command")

        if (KaitoMind.config.isDebugMode) {
            (KaitoMind.KaitoLogger.underlyingLogger as ch.qos.logback.classic.Logger)
                .level = Level.DEBUG
        }

    }


    fun intercept(bot: Bot) {
        // 消息拦截和事件拦截
        MessageManager.register(bot)
    }

    private fun shutdownTask() {
        // 退出前做数据持久处理
        DataManager.saveData()
    }
}