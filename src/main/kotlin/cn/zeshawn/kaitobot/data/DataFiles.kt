package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import java.io.IOException


object DataFiles {
    private val allDataFiles = listOf(
        ConfigsData, UserData
    )

    fun setup() {
        //加载全部数据文件
        allDataFiles.forEach {
            try {
                it.check()
            } catch (e: IOException) {
                KaitoMind.KaitoLogger.error("加载数据 ${it.file.name} 发生错误")
            }
        }

    }
}

