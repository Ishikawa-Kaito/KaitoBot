package cn.zeshawn.kaitobot

import cn.zeshawn.kaitobot.KaitoMind.kaito
import cn.zeshawn.kaitobot.core.KaitoLauncher
import cn.zeshawn.kaitobot.util.LogUtil
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess


//Start from here!
object KaitoApp {

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            KaitoLauncher.launch()
        } catch (e: Exception) {
            KaitoMind.KaitoLogger.error {
                "KaitoBot启动错误."
            }
            KaitoMind.KaitoLogger.error {
                LogUtil.formatStacktrace(e, null, true)
            }
        }

        try {
            runBlocking {
                kaito.login()
            }
            runBlocking {
                kaito.join()
            }
        } catch (e: Exception) {
            KaitoMind.KaitoLogger.error {
                "出现异常，无法正常运行。\n" +
                        LogUtil.formatStacktrace(e, null, true)
            }
            exitProcess(-1)
        }
    }
}