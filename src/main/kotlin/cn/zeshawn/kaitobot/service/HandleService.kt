package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.IdiomData
import cn.zeshawn.kaitobot.util.Save
import cn.zeshawn.kaitobot.util.fuckDrawRect
import java.awt.BasicStroke
import java.awt.BasicStroke.CAP_BUTT
import java.awt.BasicStroke.JOIN_BEVEL
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.BufferedImage.TYPE_INT_RGB
import java.time.Instant
import kotlin.random.Random

object HandleService {

    val words = buildList {
        IdiomData.idioms.forEach {
            add(it.word)
        }
    }

    fun startGame() {

    }

    fun isIdiom(answer: String): Boolean {
        return answer in words
    }
}


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


        fun singeLetter(s: String, color: Color): BufferedImage {
            return BufferedImage(CELL_SIDE, CELL_SIDE, TYPE_INT_RGB).also {
                it.createGraphics().let { g ->
                    g.background = color
                    g.clearRect(0, 0, it.width, it.height)
                    g.font = KaitoMind.font.deriveFont(75f)
                    g.color = Color.WHITE
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    val metrics = g.fontMetrics
                    val lineMetrics = metrics.getLineMetrics(s, g)
                    val width = metrics.stringWidth(s)
                    val x = (CELL_SIDE - width) / 2
                    val y = (CELL_SIDE + metrics.height) / 2 - lineMetrics.descent.toInt()
                    g.drawString(s, x, y)
                    g.dispose()
                }
            }
        }
    }


    val answer = IdiomData.idioms.random()
    val triedAnswer = mutableListOf<TriedAnswer>()
    val candidateLetters = generateCandidateLetters()

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
                            val ch = singeLetter(word[col], triedAnswer[row].colors[col])
                            g.drawImage(ch, x + 2, y + 2, null)
                        }
                    }
                }

                g.dispose()
            }
            it.Save("test.png")
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

    fun checkAnswer(answer: String) {
        val realAnswer = this.answer.getLetters()
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
        triedAnswer.add(TriedAnswer(answer, colors))
    }

    fun generateCandidateLetters(): List<String> {
        val letters = mutableSetOf<String>()
        while (letters.size < 26) {
            val idiom = IdiomData.idioms.random(Random(Instant.now().epochSecond)).word.chunked(1)
            letters.addAll(idiom)
        }
        letters.addAll(this.answer.getLetters())
        return letters.toList().shuffled()
    }


}


data class TriedAnswer(
    val word: String,
    val colors: List<Color>
)