package cn.zeshawn.kaitobot.data.base

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.util.createBackupFile
import cn.zeshawn.kaitobot.util.getContext
import java.io.File

abstract class DataFileBase(val file: File) {
    abstract fun load()// 读取
    abstract fun save()// 保存
    abstract fun init()// 初始化
    fun exists(): Boolean = file.exists()


    open fun check() {
        if (!exists() || (file.isFile && file.getContext().isEmpty())) {
            KaitoMind.KaitoLogger.info("[Data] 初始化数据 ${this.javaClass.simpleName}(${file.name})")
            init()
        }
        load()
        KaitoMind.KaitoLogger.info("[Data] 正在加载数据 ${this.javaClass.simpleName}(${file.name})")
    }

    fun createBackup() {
        file.createBackupFile()
    }

}