package cn.zeshawn.kaitobot.service

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
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
import org.bytedeco.opencv.global.opencv_core.meanStdDev
import org.bytedeco.opencv.global.opencv_imgproc.*
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.MatVector
import org.bytedeco.opencv.opencv_core.Size
import org.jetbrains.kotlinx.dl.api.extension.argmax
import java.awt.image.BufferedImage
import java.awt.image.PixelGrabber
import java.nio.DoubleBuffer
import javax.imageio.ImageIO
import kotlin.math.roundToInt


object WordleService {

    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val session: OrtSession =
        env.createSession(KaitoMind.root.absolutePath + "/data/model.onnx", OrtSession.SessionOptions())

    init {
        WordleData.load()
    }

    private var classes = lazy {
        buildList {
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ".forEach {
                for (i in 0 until 3) {
                    add("${it}_${i}")
                }
            }
        }
    }


    fun getRows(respondImage: BufferedImage): MutableList<List<Pair<String, Int>>> {
        // cv read
        val img = respondImage.toMat()
        // to grey then to binary
        val imgGrey = Mat()
        cvtColor(img, imgGrey, COLOR_RGB2GRAY)
        val binaryImg = Mat()
        threshold(imgGrey, binaryImg, 200.0, 255.0, THRESH_BINARY_INV)

        // find contours from binary img
        val cnt = MatVector()
        val hierarchy = Mat()
        // only outer contours will be added (eliminate letter contours distractions)
        findContours(binaryImg, cnt, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE)

        // general solution of binary linear equation: x * (x+1) = contours
        val length = ((-1 + kotlin.math.sqrt((1 + 4 * cnt.size()).toDouble())) / 2).roundToInt()

        // get every letter cell by its contours
        val images = buildList {
            for (i in 0 until cnt.size()) {
                val rect = boundingRect(cnt[i])
                val roi = Mat(img, rect)
                val resized = Mat()
                // resize for cnn input
                resize(roi, resized, Size(40, 40), 0.0, 0.0, INTER_LINEAR)

                // calculates std and mean to ignore blank cells
                val grey = Mat()
                cvtColor(resized, grey, COLOR_RGB2GRAY)
                val mean = Mat()
                val stdd = Mat()
                meanStdDev(grey, mean, stdd)
                val stds = stdd.createBuffer<DoubleBuffer>().get(0)
                val means = mean.createBuffer<DoubleBuffer>().get(0)
                if (stds + means > 200)
                    continue

                val image = resized.toBufferedImage()
                add(image)
            }
        }
        val rows = mutableListOf<List<Pair<String, Int>>>()
        val letters = images.reversed()
        for (i in 0 until length + 1) { // row
            val row = buildList {
                for (j in 0 until length) { // col
                    val letterIndex = i * length + j
                    if (letterIndex < letters.size) { // current cell is letter
                        // the cnn output class is {ch}_{status}
                        // status: 0-yellow 1-green 2-wrong
                        val label = predict(letters[letterIndex]).split("_")
                        val letter = label[0]
                        val status = label[1].toInt()
                        val ch = Pair(letter, status)
                        add(ch)
                    } else {// current cell is blank
                        add(Pair("", 0))
                    }
                }
            }
            rows.add(row)
        }
        return rows
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
        return solve(respondImage)
    }

    fun solve(respondImage: BufferedImage): String {
        val rows = getRows(respondImage)
        val length = getLength(rows)
        val round = getRound(rows)
        return WordleSolver().nextRound(rows, round, length)
    }


    private fun isWorldeImage(img: Image): Boolean {
        if (img.isEmoji) return false
        if (img.height - img.width > 100) {
            return false
        }

        return true
    }

    private fun predict(cell: BufferedImage): String {
        val features = imageToMatrix(cell)
        val tensor = OnnxTensor.createTensor(env, features)
        val inputs = mapOf<String, OnnxTensor>("input" to tensor)
        val result = session.run(inputs, setOf("output"))[0].value as Array<*>
        val pred = result.first() as FloatArray
        val index = pred.argmax()
        return classes.value[index]
    }

    private fun getLength(rows: MutableList<List<Pair<String, Int>>>): Int {
        return rows.size - 1
    }


    // to convert BufferedImage to float[][][][] as to fit shape(1,3,40,40)
    private fun imageToMatrix(image: BufferedImage): Array<Array<Array<FloatArray>>> {
        val width = image.width
        val height = image.height
        val pixels = IntArray(width * height)
        val pg = PixelGrabber(image, 0, 0, width, height, pixels, 0, width)
        try {
            pg.grabPixels()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val ret = Array(1) {
            Array(3) {
                Array(pg.height) {
                    FloatArray(pg.width)
                }
            }
        }
        var pixel: Int
        var row = 0
        var col = 0
        while (row * width + col < pixels.size) {
            pixel = row * width + col
            ret[0][2][row][col] = (pixels[pixel] and 0x000000FF) / 255f // blue
            ret[0][1][row][col] = (pixels[pixel] shr 8 and 0x000000FF) / 255f // green
            ret[0][0][row][col] = (pixels[pixel] shr 16 and 0x000000FF) / 255f // red
            col++
            if (col == width - 1) {
                col = 0
                row++
            }
        }
        return ret
    }


}


class WordleSolver {

    private val wrongLetters = mutableSetOf<String>() // grey letters
    private val untriedLetters = mutableSetOf<String>() // untried letters
    private val candidateWords = mutableSetOf<String>() // possible words

    private val greenLetters = mutableSetOf<Pair<String, Int>>() // green letters and position
    private val yellowLetters = mutableSetOf<Pair<String, Int>>() // yellow letters and position

    private val allWords = mutableSetOf<String>()

    private var round = 0 // which round now
    private var lastAttempt = ""  // 有记录的尝试

    init {
        reset()
    }

    private fun reset() {
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

    private fun getNextCandidateWords() {
        val removal = mutableSetOf<String>()

        candidateWords.addAll(allWords)
        allWords.forEach {
            val words = it.uppercase()
            if (isWordForbidden(words) || !matchGreen(words) || !matchYellow(words)) {
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

    private fun pickUpAWord(): String {
        getNextCandidateWords()
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
            lastAttempt = pickUpAWord()
        }
        KaitoMind.KaitoLogger.info("Round: $round Guess: $lastAttempt")
        return lastAttempt
    }

}


fun BufferedImage.toMat(): Mat {
    return ToMat().convertToMat(Java2DFrameConverter().convert(this)).clone()
}


fun Mat.toBufferedImage(): BufferedImage {
    return Java2DFrameUtils.deepCopy(Java2DFrameConverter().getBufferedImage(ToMat().convert(this).clone()))
}