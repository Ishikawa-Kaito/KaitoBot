package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import cn.zeshawn.kaitobot.entity.Config
import cn.zeshawn.kaitobot.util.loadClassFromJson
import cn.zeshawn.kaitobot.util.writeClassToJson
import java.io.File


object ConfigsData : DataFileBase(File(KaitoMind.root, "config.json")) {
    override fun load() {
        KaitoMind.config = file.loadClassFromJson()
    }

    override fun save() {
        file.writeClassToJson(KaitoMind.config)
    }

    override fun init() {
        KaitoMind.config = Config()
        save()
    }

}