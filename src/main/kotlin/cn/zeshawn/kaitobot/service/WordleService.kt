package cn.zeshawn.kaitobot.service

import cn.hutool.http.HttpUtil
import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.WordData
import cn.zeshawn.kaitobot.data.WordleData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.MessageChain
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.Java2DFrameUtils
import org.bytedeco.javacv.OpenCVFrameConverter.ToMat
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_core.CV_32SC4
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.MatVector
import org.bytedeco.opencv.opencv_core.Scalar
import org.bytedeco.opencv.opencv_core.Size
import org.jetbrains.kotlinx.dl.api.inference.onnx.OnnxInferenceModel
import org.jetbrains.kotlinx.dl.dataset.image.ColorMode
import org.jetbrains.kotlinx.dl.dataset.image.ImageConverter
import org.jetbrains.kotlinx.dl.dataset.preprocessor.Preprocessing
import org.jetbrains.kotlinx.dl.dataset.preprocessor.Preprocessor
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO


object WordleService {

    init {
        WordleData.load()
    }

    var classes = lazy {
        buildList {
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ".forEach {
                for (i in 0 until 3){
                    add("${it}_${i}")
                }
            }
        }
    }



    fun getRows(respondImage: BufferedImage): MutableList<List<Pair<String, Int>>> {
//        val rows = mutableListOf<List<Pair<String, Int>>>()
//        val lines = buildList {
//            for (i in 20..respondImage.height - 20 step 50) {
//                val tempImg = respondImage.getSubimage(20, i, respondImage.width - 40, 40)
//                add(tempImg)
//            }
//        }
//        lines.forEach {
//            val words = buildList {
//                for (i in 0..it.width step 50) {
//                    val word = it.getSubimage(i, 0, 40, 40)
//                    add(ocr(word))
//                }
//            }
//            rows.add(words)
//        }
//        return rows
        val img = respondImage.toMat()
        val grey = Mat()
        cvtColor(img,grey, COLOR_RGB2GRAY)
        val mask = Mat()
        opencv_core.inRange(
            img,
            Mat(1, 1, CV_32SC4, Scalar(118.0, 118.0, 119.0, 0.0)),
            Mat(1, 1, CV_32SC4, Scalar(128.0, 128.0, 129.0, 0.0)),
            mask
        )
        val cnt = MatVector()
        val hier = Mat()
        findContours(mask,cnt,hier,RETR_EXTERNAL,CHAIN_APPROX_SIMPLE)
        val images = buildList<BufferedImage> {
            for (i in 0 until cnt.size()){
                val rect = boundingRect(cnt[i])
                val roi = Mat(grey,rect)
                val resized = Mat()
                resize(roi,resized, Size(40,40),0.0,0.0, INTER_LINEAR)
                val image =
                    Java2DFrameUtils.deepCopy(Java2DFrameConverter().getBufferedImage(ToMat().convert(resized).clone()))

                add(image)
            }
        }
        images.reversed().forEach {
            ImageIO.write(it,"jpg", File("1.jpg"))
            println()
        }
        return mutableListOf()
    }

    private fun getRound(rows: MutableList<List<Pair<String, Int>>>): Int {
        return rows.filter { row -> row.all { it.first != "" } }.size // 获取非空行行数即为当前回合数
    }

    suspend fun solve(message: MessageChain): String {
        if (!message.contains(Image)) return ""
        val img = message.first { it is Image } as Image
        if (!isWorldeImage(img)) return ""
        val respondImage = withContext(Dispatchers.IO) {
            ImageIO.read(HttpUtil.downloadBytes(img.queryUrl()).inputStream())
        }
        val length = getLength(respondImage)
        val rows = getRows(respondImage)
        val round = getRound(rows)
        return WordleSolver().nextRound(rows, round, length)
    }

    private fun compareByte(imgA: BufferedImage, imgB: BufferedImage): Boolean {
        var diff = 0
        for (i in 0 until imgA.width) {
            for (j in 0 until imgA.height) {
                if (imgA.getRGB(i, j) != imgB.getRGB(i, j))
                    diff++
                if (diff > 10) return false
            }
        }

        return true
    }

    private fun isWorldeImage(img: Image): Boolean {
        if (img.isEmoji) return false
        if (img.height - img.width != 50) {
            return false
        }

        return true
    }

    private fun ocr(img: BufferedImage): Pair<String, Int> {
        WordleData.charData.keys.forEach {
            if (compareByte(it, img)) {
                return WordleData.charData[it]!!
            }
        }
        return Pair("", 0)
    }


    fun predict(cell:BufferedImage){
        OnnxInferenceModel.load("KaitoWordleOcr v1.0.onnx").use {
            val input = ImageConverter.toRawFloatArray(cell,ColorMode.GRAYSCALE)
            val out = it.predict(input)
            println(classes.value[out])
        }
    }

    fun BufferedImage.toByteArray(): ByteArray {
        val out = ByteArrayOutputStream()
        ImageIO.write(this, "PNG", out)
        return out.toByteArray()
    }

    private fun getLength(respondImage: BufferedImage): Int {
        // imageWidth = letterWidth * length + (length-1) * padding + boarder
        // padding = 10 letterWidth = 40 boarder = 40
        return (respondImage.width - 30) / 50
    }

}


class WordleSolver {

    private val wrongLetters = mutableSetOf<String>() // grey letters
    private val untriedLetters = mutableSetOf<String>() // untried letters
    private val candidateWords = mutableSetOf<String>() // possible words

    private val greenLetters = mutableSetOf<Pair<String, Int>>() // green letters and position
    private val yellowLetters = mutableSetOf<Pair<String, Int>>() // yellow letters and position

    private val allWords = mutableSetOf<String>()

    var round = 0 // which round now
    var lastAttempt = ""  // 有记录的尝试

    init {
        reset()
    }

    fun reset() {
        wrongLetters.clear()
        untriedLetters.addAll("ABCDEFGHIJKLMNOPQRSTUVWXYZ".lowercase().chunked(1))
        candidateWords.clear()
        round = 0
        lastAttempt = ""
    }


    private fun getLetterCounter(valid: Boolean, words: MutableSet<String>): Map<String, Int> {
        val probabilities = mutableMapOf<String, Int>()
        words.forEach {
            it.forEach { char ->
                val letter = char.toString()
                if (valid || letter in untriedLetters) {
                    if (probabilities.contains(letter)) {
                        probabilities[letter] = probabilities[letter]!!.plus(1)
                    } else {
                        probabilities[letter] = 1
                    }
                }
            }
        }
        return probabilities
    }


    /**
     * 获得所有字母的词频
     */
    private fun getLetterFreq(words: MutableSet<String>): Map<String, Int> {
        return getLetterCounter(true, words)
    }

    /**
     * 获得有可能的字母的词频
     */
    private fun getLetterProbe(words: MutableSet<String>): Map<String, Int> {
        return getLetterCounter(false, words)
    }

    private fun isWordForbidden(word: String): Boolean {
        return word.any { it.toString() in wrongLetters }
    }

    private fun matchGreen(word: String): Boolean {
        return greenLetters.all {
            it.first == word[it.second - 1].toString()
        }
    }

    private fun matchYellow(word: String): Boolean {
        return yellowLetters.all {
            it.first != word[it.second - 1].toString() && it.first in word
        }
    }

    private fun getNextCandidateWords(length: Int) {
        val removal = mutableSetOf<String>()

        candidateWords.addAll(allWords)
        allWords.forEach {
            if (isWordForbidden(it) || !matchGreen(it) || !matchYellow(it)) {
                removal.add(it)
            }
        }
        candidateWords.removeAll(removal)
    }

    private fun guess(): String {
        val probes = getLetterProbe(candidateWords)
        val freq = getLetterFreq(candidateWords)
        if (untriedLetters.size > 1 && round < 6) {
            val wordScore = mutableListOf<Triple<String, Int, Int>>()
            val wordList = candidateWords
            wordList.forEach { word ->
                val letters = word.chunked(1).toSet()
                val untriedScore = letters.sumOf {
                    probes[it] ?: 0
                }
                val freqScore = letters.sumOf {
                    freq[it] ?: 0
                }
                wordScore.add(Triple(word, untriedScore, freqScore))
            }
            return wordScore.sortedWith(compareBy({ -it.second }, { -it.third }, { it.first }))[0].first
        } else {
            return candidateWords.sortedWith(
                compareBy(
                    { -it.chunked(1).toSet().size },
                    {
                        -it.sumOf { letter ->
                            freq[letter.toString()]!!
                        }
                    }
                )
            )[0]
        }
    }

    private fun pickUpAWord(length: Int): String {
        getNextCandidateWords(length)
        println("left ${candidateWords.size} words")

        if (candidateWords.isEmpty()) {
            println("黔驴技穷")
            return ""
        } else if (candidateWords.size == 1) {
            return candidateWords.first()
        }

        return guess()
    }

    // 获取游戏状态
    private fun isWinning(rows: MutableList<List<Pair<String, Int>>>): Boolean {
        return rows.any { row -> row.all { it.second == 1 } } // 任意一行全绿判定为胜利
    }

    private fun collectInfos(rows: MutableList<List<Pair<String, Int>>>, length: Int) {
        for (row in rows.filter { row -> row.all { it.first != "" } }) {
            for (i in 0 until length) {
                // 0-yellow 1-green 2-wrong
                when (row[i].second) {
                    0 -> {
                        yellowLetters.add(Pair(row[i].first, i + 1))
                    }
                    1 -> {
                        val letterV = Pair(row[i].first, i + 1)
                        greenLetters.add(letterV)
                        yellowLetters.removeIf { it == letterV }
                    }
                    2 -> {
                        wrongLetters.add(row[i].first)
                    }
                }
                untriedLetters.removeIf { it.equals(row[i]) }
            }
        }
    }

    fun nextRound(rows: MutableList<List<Pair<String, Int>>>, round: Int, length: Int): String {
        KaitoMind.KaitoLogger.info("Round: $round Guess $lastAttempt")
        this.round = round
        this.allWords.clear()
        this.allWords.addAll(WordData.getWordsByLength(length))
        if (this.round == 0) { // 初始回合
            reset()
        }
        if (isWinning(rows)) { // 胜利的话就重置状态
            this.reset()
            return "拿下了"
        } else { // 收集green yellow untried
            collectInfos(rows, length)
            lastAttempt = pickUpAWord(length)
        }
        KaitoMind.KaitoLogger.info("Round: $round Guess: $lastAttempt")
        return lastAttempt
    }

}



fun BufferedImage.toMat():Mat {
    return ToMat().convertToMat(Java2DFrameConverter().convert(this)).clone()
}