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
    fun getLetters(): List<String> {
        return this.word.chunked(1)
    }

    fun getPinyin(): List<Pinyin> {
        return buildList<Pinyin> {
            pinyin.split(" ").forEach { it ->
                val phonetic = it.filter { ch -> ch.isLetter() }
                val tone = it.filter { ch -> ch.isDigit() }.toInt()
                add(Pinyin(phonetic, tone))
            }
        }
    }

    fun check(answer: Idiom): List<Int> {
        val a = this.getLetters()
        val b = answer.getLetters()
        return buildList {
            for (i in a.indices) {
                val res = when (b[i]) {
                    a[i] -> 1
                    in a -> 0
                    else -> 2
                }
                add(i, res)
            }
        }
    }
}


data class Pinyin(
    val phonetic: String,
    val tone: Int
)