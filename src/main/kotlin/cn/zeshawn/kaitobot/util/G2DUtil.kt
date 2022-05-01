package cn.zeshawn.kaitobot.util

import java.awt.FontMetrics
import java.awt.Graphics

fun Graphics.fuckDrawRect(x: Int, y: Int, width: Int, height: Int, bold: Int) {
    for (b in 0 until bold) {
        this.drawRect(x + b, y + b, width + 2 * (bold - b - 1) + 1, height + 2 * (bold - b - 1) + 1)
    }
}


fun Graphics.fuckDrawString(str: String, maxWidth: Int, x: Int, y: Int, dy: Int) {
    val metric = this.fontMetrics
    val res = getTextLines(metric, str, maxWidth)
    for (i in res.indices) {
        this.drawString(res[i], x, y + dy * i)
    }
}


fun getTextLines(fg: FontMetrics, text: String, width: Int): MutableList<String> {
    var i = 1
    var temp = text
    val res = mutableListOf<String>()
    while (true) {
        if (fg.stringWidth(temp) > width) {
            temp = temp.substring(0, text.length - 1)
            i++
        } else {
            break
        }
    }
    if (i != 1) {
        res.add(text.substring(0, text.length - i))
        res.addAll(getTextLines(fg, text.substring(text.length - i), width))
    } else {
        res.add(text)
    }
    return res
}