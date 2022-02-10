package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import cn.zeshawn.kaitobot.util.loadClassFromJson
import cn.zeshawn.kaitobot.util.writeClassToJson
import java.io.File

object GroupData : DataFileBase(File(KaitoMind.root, "group.json")) {
    override fun load() {
        KaitoMind.groups.putAll(file.loadClassFromJson())
    }

    override fun save() {
        file.writeClassToJson(KaitoMind.groups)
    }

    override fun init() {
        save()
    }

}