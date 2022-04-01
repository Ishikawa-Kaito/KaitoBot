package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import cn.zeshawn.kaitobot.listener.ChatManager
import cn.zeshawn.kaitobot.util.loadClassFromJson
import cn.zeshawn.kaitobot.util.writeClassToJson
import com.fasterxml.jackson.databind.deser.Deserializers
import java.io.File

object ChatLibraryData : DataFileBase(File(KaitoMind.root, "chatLibrary.json")) {
    override fun load() {
        ChatManager.chatLibrary.putAll(file.loadClassFromJson())
    }

    override fun save() {
        file.writeClassToJson(ChatManager.chatLibrary)
    }

    override fun init() {
        save()
    }
}

class MessageChainDeserializer : Deserializers.Base() {

}



