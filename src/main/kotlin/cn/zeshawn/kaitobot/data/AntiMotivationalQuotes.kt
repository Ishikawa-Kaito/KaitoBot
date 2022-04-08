package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import com.fasterxml.jackson.databind.JsonNode
import java.io.File


object AntiMotivationalQuotes : DataFileBase(File("${KaitoMind.root}/data", "anti_motivational_quotes.json")) {

    lateinit var quotes: JsonNode

    override fun load() {
        quotes = KaitoMind.mapper.readTree(this.file)

    }

    override fun save() {

    }

    override fun init() {

    }


}

