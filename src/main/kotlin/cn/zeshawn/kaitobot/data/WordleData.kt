package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object WordleData: DataFileBase(KaitoMind.root.resolve("data/wordle"))  {
    val charData:MutableMap<BufferedImage,Pair<String,Int>> = mutableMapOf()

    override fun load() {
        if (file.exists() && file.isDirectory){
            file.listFiles()!!.forEach {
                if (it.extension.lowercase() == "png") {
                    ImageIO.read(it).let { image ->
                        val charMeta = it.name.chunked(1)
                        charData[image] = Pair(charMeta[0],charMeta[1].toInt())
                    }
                }
            }
        }
    }

    override fun save() {
    }

    override fun init() {
    }
}