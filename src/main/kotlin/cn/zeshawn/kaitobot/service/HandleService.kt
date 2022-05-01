package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.Idiom
import cn.zeshawn.kaitobot.data.IdiomData
import cn.zeshawn.kaitobot.data.Pinyin
import cn.zeshawn.kaitobot.util.Save
import cn.zeshawn.kaitobot.util.fuckDrawRect
import java.awt.BasicStroke
import java.awt.BasicStroke.CAP_BUTT
import java.awt.BasicStroke.JOIN_BEVEL
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.time.Instant
import kotlin.random.Random

class HandleGame {

    companion object {
        private const val WIDTH = 600
        private const val HEIGHT = 900
        private const val CELL_SIDE = 120
        private const val CELL_PADDING = 10
        private const val BOARDER_WIDTH = 2
        private const val BOARDER_PADDING = (WIDTH - (CELL_SIDE * 4 + CELL_PADDING * 6 + BOARDER_WIDTH * 8)) / 2
        private val BOARDER_COLOR = Color(204, 204, 204)
        private val CORRECT_COLOR = Color(32, 176, 161)
        private val WRONG_COLOR = Color(128, 128, 128)
        private val CANDIDATE_COLOR = Color(255, 165, 0)

        private val words = buildList {
            IdiomData.idioms.forEach {
                add(it.word)
            }
        }

        fun isIdiom(answer: String): Boolean {
            return answer in words
        }

        fun singeLetter(s: String, pinyin: Pinyin, color: Color, pinyinColor: List<Color>): BufferedImage {
            return BufferedImage(CELL_SIDE, CELL_SIDE, TYPE_INT_RGB).also {
                it.createGraphics().let { g ->
                    g.background = Color(225, 225, 225)
                    g.clearRect(0, 0, it.width, it.height)
                    g.font = KaitoMind.GenYoMinFont.deriveFont(65f)
                    g.color = color
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    val metrics = g.fontMetrics
                    val lineMetrics = metrics.getLineMetrics(s, g)
                    val width = metrics.stringWidth(s)
                    val x = (CELL_SIDE - width) / 2
                    val y = (CELL_SIDE + metrics.height) / 2 - lineMetrics.descent.toInt() + 10
                    g.drawString(s, x, y)
                    g.font = KaitoMind.InconsolataFont.deriveFont(Font.BOLD, 25f)
                    val fg = g.fontMetrics
                    val pyStr = pinyin.ShengMu + pinyin.YunMu + pinyin.tone.toString()
                    val lg = fg.getLineMetrics(pyStr, g)
                    val pyWidth = fg.stringWidth(pyStr)
                    var dx = (width - pyWidth) / 2
                    val dy = 2 * fg.height + fg.descent + lg.descent.toInt()
                    g.color = pinyinColor[0]
                    g.drawString(pinyin.ShengMu, x + dx, y - dy)
                    dx += fg.stringWidth(pinyin.ShengMu)
                    g.color = pinyinColor[1]
                    g.drawString(pinyin.YunMu, x + dx, y - dy)
                    dx += fg.stringWidth(pinyin.YunMu)
                    g.color = pinyinColor[2]
                    g.drawString(pinyin.tone.toString(), x + dx, y - dy)
                    g.dispose()
                }
            }
        }
    }


    val answer = IdiomData.idioms.random()
    private val triedAnswer = mutableListOf<TriedAnswer>()
    val candidateLetters = generateCandidateLetters()

    val isOver
        get() = triedAnswer.size >= 6

    val tips = lazy {
        val i = Random(Instant.now().epochSecond).nextInt(4)
        return@lazy "第${i + 1}个位置的拼音是${this.answer.getPinyin()[i]}"
    }

    fun draw(): BufferedImage {
        return BufferedImage(WIDTH, HEIGHT, TYPE_INT_RGB).also {
            it.createGraphics().let { g ->
                g.background = Color.WHITE
                g.clearRect(0, 0, WIDTH, HEIGHT)
                val start = BOARDER_PADDING
                val endX = WIDTH - CELL_SIDE - BOARDER_PADDING
                val endY = 5 * CELL_SIDE + 10 * CELL_PADDING + BOARDER_PADDING + 10 * BOARDER_WIDTH
                val steps = CELL_SIDE + 2 * CELL_PADDING + 2 * BOARDER_WIDTH

                (start..endY step steps).forEachIndexed { row, y ->
                    val word = if (row < this.triedAnswer.size) {
                        triedAnswer[row].word.chunked(1)
                    } else {
                        listOf()
                    }
                    (start..endX step steps).forEachIndexed { col, x ->
                        g.stroke = BasicStroke(1.0f, CAP_BUTT, JOIN_BEVEL)
                        g.color = BOARDER_COLOR
                        g.fuckDrawRect(x, y, CELL_SIDE, CELL_SIDE, BOARDER_WIDTH)
                        if (col < word.size) {
                            val ch = singeLetter(
                                word[col],
                                triedAnswer[row].pinyin[col],
                                triedAnswer[row].colors[col],
                                triedAnswer[row].pinyinColor.subList(col * 3, (col + 1) * 3)
                            )
                            g.drawImage(ch, x + BOARDER_WIDTH, y + BOARDER_WIDTH, null)
                        }
                    }
                }
                g.dispose()
            }
        }
    }

    fun attempt(tryAnswer: String): Boolean {
        if (tryAnswer.length >= 6) return false
        checkAnswer(tryAnswer)
        if (tryAnswer == this.answer.word) {
            return true
        }
        return false
    }

    private fun checkAnswer(answer: String) {
        val tryAnswer = Idiom.getIdiom(answer)!!
        val realAnswer = this.answer.letters
        val colors = buildList {
            answer.chunked(1).forEachIndexed { index, s ->
                add(
                    when (s) {
                        realAnswer[index] -> CORRECT_COLOR
                        in realAnswer -> CANDIDATE_COLOR
                        else -> WRONG_COLOR
                    }
                )
            }
        }
        val tryPinyin = tryAnswer.getPinyin()
        val realPinyin = this.answer.getPinyin()
        val pinyinColors = buildList {
            for (i in tryPinyin.indices) {
                when (tryPinyin[i].ShengMu) {
                    realPinyin[i].ShengMu -> add(CORRECT_COLOR)
                    in buildList { realPinyin.forEach { add(it.ShengMu) } } -> add(CANDIDATE_COLOR)
                    else -> add(WRONG_COLOR)
                }
                when (tryPinyin[i].YunMu) {
                    realPinyin[i].YunMu -> add(CORRECT_COLOR)
                    in buildList { realPinyin.forEach { add(it.YunMu) } } -> add(CANDIDATE_COLOR)
                    else -> add(WRONG_COLOR)
                }
                when (tryPinyin[i].tone) {
                    realPinyin[i].tone -> add(CORRECT_COLOR)
                    in buildList { realPinyin.forEach { add(it.tone) } } -> add(CANDIDATE_COLOR)
                    else -> add(WRONG_COLOR)
                }
            }
        }
        triedAnswer.add(TriedAnswer(answer, tryPinyin, colors, pinyinColors))
    }

    private fun generateCandidateLetters(): List<String> {
        // 生成一组可选字
        val letters = mutableSetOf<String>()
        letters.addAll(answer.letters)
        val parts = buildList {
            for (ch in answer.letters) {
                add(IdiomData.idioms.filter { ch in it.letters })
            }
        }
        while (letters.size < 20) {
            parts.forEach {
                letters.addAll(it.random(Random(Instant.now().epochSecond)).letters)
            }
        }
        return letters.toList().shuffled()
    }


    fun isAnswerValid(answer: String): Boolean {
        return !triedAnswer.any { it.word == answer }
    }


    fun generateResult(): BufferedImage {
        val width = WIDTH * 3 / 2
        val height = HEIGHT * 2 / 3
        val padding = (width - (CELL_SIDE * 4 + CELL_PADDING * 6 + BOARDER_WIDTH * 8)) / 2
        return BufferedImage(width, height, TYPE_INT_RGB).also {
            it.createGraphics().let { g ->
                g.background = Color.WHITE
                g.clearRect(0, 0, it.width, it.height)
                val endX = padding + 6 * (BOARDER_WIDTH + CELL_PADDING) + 3 * CELL_SIDE
                val steps = CELL_SIDE + 2 * CELL_PADDING + 2 * BOARDER_WIDTH
                val pinyins = answer.getPinyin()
                (padding..endX step steps).forEachIndexed { col, x ->
                    g.stroke = BasicStroke(1.0f, CAP_BUTT, JOIN_BEVEL)
                    g.color = BOARDER_COLOR
                    g.fuckDrawRect(x, padding, CELL_SIDE, CELL_SIDE, BOARDER_WIDTH)
                    if (col < answer.letters.size) {
                        val ch =
                            singeLetter(answer.letters[col], pinyins[col], CORRECT_COLOR, List(12) { CORRECT_COLOR })
                        g.drawImage(ch, x + BOARDER_WIDTH, padding + BOARDER_WIDTH, null)
                    }
                }
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g.color = Color.black
                g.font = KaitoMind.GenYoMinFont.deriveFont(Font.BOLD, 30f)
                val fg = g.fontMetrics
                val maxWidth = width - padding

                // 多行文字前面加点空格
                val str = if (answer.derivation == "无") "啊没有典故捏。" else answer.derivation.run {
                    if (fg.stringWidth(this) > maxWidth) "   $this" else this
                }
                val lines = mutableListOf<String>()
                var start = 0

                // 文字换行
                while (true) {
                    var curPos = 0
                    while (true) {
                        if (fg.stringWidth(str.substring(start, str.length - curPos)) < maxWidth) {
                            lines.add(str.substring(start, str.length - curPos))
                            start = str.length - curPos
                            break
                        } else {
                            curPos++
                        }
                    }
                    if (curPos == 0) break
                }
                for (i in lines.indices) {
                    g.drawString(
                        lines[i],
                        padding / 2,
                        padding * 5 / 2 + (g.fontMetrics.height + g.fontMetrics.descent + g.fontMetrics.ascent) * i
                    )
                }
                g.dispose()
            }
        }
    }


}


data class TriedAnswer(
    val word: String,
    val pinyin: List<Pinyin>,
    val colors: List<Color>,
    val pinyinColor: List<Color>
)