package cn.zeshawn.kaitobot.data

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.base.DataFileBase
import java.io.File

object IdiomData : DataFileBase(File("${KaitoMind.root}/data", "idioms.json")) {
    lateinit var idioms: List<Idiom>
    override fun load() {
        val jsons = KaitoMind.mapper.readTree(this.file)
        idioms = buildList {
            jsons.forEach {
                val word = it["word"].asText()
                if (word.length != 4) return@forEach
                val pinyin = it["pinyin"].asText()
                val derivation = it["derivation"].asText()
                val explanation = it["explanation"].asText()
                add(Idiom(word, pinyin, derivation, explanation))
            }
        }

    }

    override fun save() {

    }

    override fun init() {

    }
}


class Idiom(
    val word: String,
    val pinyin: String,
    val derivation: String,
    val explanation: String
) {

    companion object {


        fun getIdiom(text: String): Idiom? {
            return IdiomData.idioms.firstOrNull { it.word == text }
        }

    }


    val letters = initLetters()

    private fun initLetters(): List<String> {
        return this.word.chunked(1)
    }


    fun getPinyin(): List<Pinyin> {
        return buildList<Pinyin> {
            pinyin.split(" ").forEach {
                val possibleShengmu = Pinyin.ShengMuList.filter { shengmu -> it.startsWith(shengmu) }
                    .sortedBy { shengmu -> shengmu.length }
                val shengmu = if (possibleShengmu.isEmpty()) "" else possibleShengmu.last()
                val yunmu = Pinyin.deTone(it.removePrefix(shengmu))
                val toneSymbol = it.chunked(1).first { ch -> ch in Pinyin.toneMap.keys }
                val tone = Pinyin.toneMap[toneSymbol]!!
                add(Pinyin(shengmu, yunmu, tone))
            }
        }
    }
}


class Pinyin(
    val ShengMu: String,
    val YunMu: String,
    val tone: Int
) {
    companion object {
        val YuanYin = "āáǎăà ēéěĕè īíǐĭì ōŏóǒò ūúǔŭù ǖǘǚǜ".split(" ")
        val YuanYinRaw = "a e i o u ü".split(" ")
        val ShengMuList = "b p m f d t n l g k h j q x zh ch sh r z c s y w".split(" ")
        val toneMap = mapOf(
            "ā" to 1, "á" to 2, "ă" to 3, "ǎ" to 3, "à" to 4,
            "ē" to 1, "é" to 2, "ĕ" to 3, "ě" to 3, "è" to 4,
            "ī" to 1, "í" to 2, "ǐ" to 3, "ĭ" to 3, "ì" to 4,
            "ō" to 1, "ó" to 2, "ǒ" to 3, "ŏ" to 3, "ò" to 4,
            "ū" to 1, "ú" to 2, "ǔ" to 3, "ŭ" to 3, "ù" to 4,
            "ǖ" to 1, "ǘ" to 2, "ǚ" to 3, "ǜ" to 4,
        )

        // 去除声调
        fun deTone(toned: String): String {

            val yuanyin = toned.first {
                it in YuanYin.joinToString("")
            }.toString()
            val indexYuanYin = YuanYin.indexOfFirst { yuanyin in it }
            return toned.replace(yuanyin, YuanYinRaw[indexYuanYin])
        }

    }

    override fun equals(other: Any?): Boolean {
        if (other !is Pinyin) return false
        return other.ShengMu == this.ShengMu && other.YunMu == this.YunMu && other.tone == this.tone
    }

    override fun hashCode(): Int {
        var result = ShengMu.hashCode()
        result = 31 * result + YunMu.hashCode()
        result = 31 * result + tone
        return result
    }

    override fun toString(): String {
        return ShengMu + YunMu + tone.toString()
    }
}