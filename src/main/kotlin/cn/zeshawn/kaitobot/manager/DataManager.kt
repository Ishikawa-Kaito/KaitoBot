package cn.zeshawn.kaitobot.manager

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import cn.zeshawn.kaitobot.util.LogUtil
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder


object DataManager {
    private lateinit var allDataFiles: List<DataFileBase>

    fun setup() {
        KaitoMind.KaitoLogger.info("[Data] 开始自动加载数据文件")
        val reflection = Reflections(ConfigurationBuilder().forPackage("cn.zeshawn.kaitobot.data"))
        allDataFiles =
            reflection.getSubTypesOf(DataFileBase::class.java)
                .map { it.kotlin }
                .mapNotNull { it.objectInstance }
                .toTypedArray().toList()

        //加载全部数据文件
        allDataFiles.forEach {
            try {
                it.check()
            } catch (e: Exception) {
                KaitoMind.KaitoLogger.error("加载数据 ${it.file.name} 发生错误")
                KaitoMind.KaitoLogger.error {
                    LogUtil.formatStacktrace(e, null, true)
                }
            }
        }

    }

    fun saveData() {
        allDataFiles.forEach {
            it.save()
        }
    }
}

