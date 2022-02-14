package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.data.AbstractWordData
import com.huaban.analysis.jieba.JiebaSegmenter
import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType

object AbstractWordService {

    private fun getPinyin(ch: Char): String {
        val format = HanyuPinyinOutputFormat().also {
            it.toneType = HanyuPinyinToneType.WITHOUT_TONE
        }
        return try {
            PinyinHelper.toHanyuPinyinStringArray(ch, format)[0]
        } catch (e: Exception) {
            ""
        }
    }

    fun getPinyin(word: String): String {
        return buildString {
            word.forEach {
                append(getPinyin(it))
            }
        }
    }

    private fun getWords(sentence: String): MutableList<String> {
        val segmenter = JiebaSegmenter()
        return segmenter.process(sentence, JiebaSegmenter.SegMode.INDEX).map {
            it.word.trim()
        }.toMutableList()
    }

    fun getAbstract(sentence: String): String {
        val words = getWords(sentence)
        return buildString {
            for (word in words) {
                if (word in AbstractWordData.rawDict.keys) {
                    append(AbstractWordData.rawDict[word])
                } else {
                    val wordPinyin = getPinyin(word)
                    if (wordPinyin in AbstractWordData.pinyinDict.keys) {
                        append(AbstractWordData.pinyinDict[wordPinyin])
                    } else {
                        if (word.isNotEmpty()) {
                            for (char in word) {
                                if (char.toString() in AbstractWordData.rawDict.keys) {
                                    append(AbstractWordData.rawDict[char.toString()])
                                } else {
                                    val charPinyin = getPinyin(char)
                                    if (charPinyin in AbstractWordData.pinyinDict.keys) {
                                        append(AbstractWordData.pinyinDict[charPinyin])
                                    } else {
                                        append(char)
                                    }
                                }
                            }
                        } else {
                            append(word)
                        }
                    }
                }
            }
        }

    }
}