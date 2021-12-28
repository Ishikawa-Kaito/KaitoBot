package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import cn.zeshawn.kaitobot.util.loadClassFromJson
import cn.zeshawn.kaitobot.util.writeClassToJson
import java.io.File

object UserData : DataFileBase(File(KaitoMind.root, "user.json")) {
    override fun load() {
        KaitoMind.users.putAll(file.loadClassFromJson())
    }

    override fun save() {
        file.writeClassToJson(KaitoMind.users)
    }

    override fun init() {
        save()
    }

}