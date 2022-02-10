package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.entity.Group
import com.huaban.analysis.jieba.JiebaSegmenter
import com.kennycason.kumo.CollisionMode
import com.kennycason.kumo.WordCloud
import com.kennycason.kumo.bg.CircleBackground
import com.kennycason.kumo.font.FontWeight
import com.kennycason.kumo.font.KumoFont
import com.kennycason.kumo.font.scale.LinearFontScalar
import com.kennycason.kumo.nlp.FrequencyAnalyzer
import com.kennycason.kumo.nlp.tokenizer.api.WordTokenizer
import com.kennycason.kumo.palette.ColorPalette
import java.awt.Color
import java.awt.Dimension
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object WordCloudService {

    fun getWordCloud(id: Long): ByteArrayInputStream {
        val frequencyAnalyzer = FrequencyAnalyzer()
        frequencyAnalyzer.let {
            it.setWordFrequenciesToReturn(300)
            it.setMinWordLength(2)
            it.setWordTokenizer(JieBaTokenizer())
        }
        val frequencies = frequencyAnalyzer.load(Group.getGroupOrAdd(id).wordList)
        val dimension = Dimension(1000, 1000)
        val wordCloud = WordCloud(dimension, CollisionMode.PIXEL_PERFECT).also {
            it.setPadding(2)
            it.setBackground(CircleBackground(500))
            it.setBackgroundColor(Color(0xFFFFFF))
            it.setKumoFont(KumoFont("黑体", FontWeight.PLAIN))
            it.setColorPalette(
                ColorPalette(
                    listOf(
                        "0000FF",
                        "40D3F1",
                        "40C5F1",
                        "40AAF1",
                        "408DF1",
                        "4055F1"
                    ).map { c -> c.toIntOrNull(16)?.let { it1 -> Color(it1) } })
            )
            it.setFontScalar(LinearFontScalar(10, 40))
        }
        wordCloud.build(frequencies)
        val stream = ByteArrayOutputStream()
        wordCloud.writeToStreamAsPNG(stream)
        return ByteArrayInputStream(stream.toByteArray())
    }
}

class JieBaTokenizer : WordTokenizer {
    override fun tokenize(sentence: String?): MutableList<String> {
        val segmenter = JiebaSegmenter()
        return segmenter.process(sentence, JiebaSegmenter.SegMode.INDEX).map {
            it.word.trim()
        }.toMutableList()
    }


}