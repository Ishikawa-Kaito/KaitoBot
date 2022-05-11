package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.data.WordData
import cn.zeshawn.kaitobot.util.fuckDrawRect
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage


class WordleGame {

    companion object {
        private const val WIDTH = 500
        private const val HEIGHT = 600
        private const val CELL_SIDE = 60
        private const val CELL_PADDING = 10
        private const val BOARDER_WIDTH = 2
        private const val BOARDER_PADDING = (WIDTH - (CELL_SIDE * 5 + CELL_PADDING * 8 + BOARDER_WIDTH * 10)) / 2
        private val BOARDER_COLOR = Color(204, 204, 204)
        private val CORRECT_COLOR = Color(32, 176, 161)
        private val WRONG_COLOR = Color(128, 128, 128)
        private val CANDIDATE_COLOR = Color(255, 165, 0)

        private val words = WordData.getWordsByLength(5).map { word -> word.uppercase() }

        fun singeLetter(s: String, color: Color): BufferedImage {
            return BufferedImage(CELL_SIDE, CELL_SIDE, BufferedImage.TYPE_INT_RGB).also {
                it.createGraphics().let { g ->
                    g.background = color
                    g.clearRect(0, 0, it.width, it.height)
                    g.font = KaitoMind.Rajdhani.deriveFont(Font.BOLD, 40f)
                    g.color = Color.WHITE
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
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

    val answer = words.random().uppercase()
    private val triedAnswer = mutableListOf<TriedWord>()
    val isOver
        get() = triedAnswer.size >= 6
    val triedTimes
        get() = triedAnswer.size

    fun draw(): BufferedImage {
        return BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB).also {
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
                        g.stroke = BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL)
                        g.color = BOARDER_COLOR
                        g.fuckDrawRect(x, y, CELL_SIDE, CELL_SIDE, BOARDER_WIDTH)
                        if (col < word.size) {
                            val ch = singeLetter(
                                word[col],
                                triedAnswer[row].colors[col]
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
        if (isOver) return false
        checkAnswer(tryAnswer)
        if (tryAnswer == this.answer) {
            return true
        }
        return false
    }

    private fun checkAnswer(answer: String) {
        val realAnswer = this.answer
        val colors = buildList {
            answer.forEachIndexed { index, s ->
                add(
                    when (s) {
                        realAnswer[index] -> CORRECT_COLOR
                        in realAnswer -> CANDIDATE_COLOR
                        else -> WRONG_COLOR
                    }
                )
            }
        }
        triedAnswer.add(TriedWord(answer, colors))
    }

    fun isAnswerUsed(answer: String): Boolean {
        return !triedAnswer.any { it.word == answer }
    }

    fun isAnswerValid(answer: String): Boolean {
        return answer in words
    }

}


data class TriedWord(
    val word: String,
    val colors: List<Color>,
)