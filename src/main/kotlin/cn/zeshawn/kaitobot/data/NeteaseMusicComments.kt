package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import cn.zeshawn.kaitobot.util.loadClassFromJson
import java.io.File

object NeteaseMusicComments : DataFileBase(File(KaitoMind.root, "163MusicComments.json")) {
    override fun load() {
        KaitoMind.music163Comments.addAll(file.loadClassFromJson())
    }

    override fun save() {

    }

    override fun init() {

    }

    override fun check() {
        if (file.exists()) {
            load()
            KaitoMind.KaitoLogger.info("[Data] 正在加载数据 ${this.javaClass.simpleName}(${file.name})")
        }
    }
}