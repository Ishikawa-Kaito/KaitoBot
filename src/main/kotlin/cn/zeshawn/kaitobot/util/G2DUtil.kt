package cn.zeshawn.kaitobot.util

import java.awt.Graphics

fun Graphics.fuckDrawRect(x: Int, y: Int, width: Int, height: Int, bold: Int) {
    for (b in 0 until bold) {
        this.drawRect(x + b, y + b, width + 2 * (bold - b - 1) + 1, height + 2 * (bold - b - 1) + 1)
    }
}