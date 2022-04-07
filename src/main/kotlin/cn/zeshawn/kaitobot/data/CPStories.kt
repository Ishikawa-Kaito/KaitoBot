package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import com.fasterxml.jackson.databind.JsonNode
import java.io.File


object CPStories : DataFileBase(File("${KaitoMind.root}/data", "cp.json")) {

    lateinit var stories: JsonNode

    override fun load() {
        stories = KaitoMind.mapper.readTree(this.file)

    }

    override fun save() {

    }

    override fun init() {

    }


}

