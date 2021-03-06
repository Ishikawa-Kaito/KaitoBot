package cn.zeshawn.kaitobot.service

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO


/***
 * idea comes from https://github.com/ddiu8081/oreooo
 * kotlin implements edition
 */

object OreoService {
    private val Oimg = ImageIO.read(File("./data/Oreo/O.png")).getScaledInstance(240, 160, Image.SCALE_SMOOTH)
    private val Obimg = ImageIO.read(File("./data/Oreo/Ob.png")).getScaledInstance(240, 160, Image.SCALE_SMOOTH)
    private val Rimg = ImageIO.read(File("./data/Oreo/R.png")).getScaledInstance(220, 155, Image.SCALE_SMOOTH)
    private val layerMap = mapOf("O" to Oimg, "Ob" to Obimg, "R" to Rimg)

    fun generateOreo(oreoString: String): InputStream? {
        if (oreoString.isEmpty()) return null
        val oreoStr = if (oreoString.length > 100) oreoString.substring(0, 100) else oreoString
        val oreoArr = mutableListOf<String>()
        val drawArr = mutableListOf<DrawItem>()
        for (layer in oreoStr.chunked(1)) {
            val layerToAdd = when (layer) {
                "奥" -> if (oreoArr.isEmpty() || oreoArr.last() == "-") "O" else "Ob"
                "利" -> "R"
                "与", "和" -> if (oreoArr.isNotEmpty() && oreoArr.last() != "-") "-" else ""
                else -> ""
            }
            if (layerToAdd.isNotEmpty())
                oreoArr.add(layerToAdd)
        }
        if (oreoArr.isEmpty()) return null
        if (oreoArr.last() == "-") oreoArr.removeLast()
        var height = 0
        for (layer in oreoArr) {
            height += if (layer != "-") {
                drawArr.add(
                    DrawItem(
                        type = layer,
                        x = if (layer == "R") 10 else 0,
                        y = height,
                        width = if (layer == "R") 220 else 240,
                        height = if (layer == "R") 155 else 160
                    )
                )
                24
            } else {
                72
            }
        }
        height += 160 - 24
        val image = BufferedImage(240, height, BufferedImage.TYPE_4BYTE_ABGR)
        val g2d = image.createGraphics()
        drawArr.reversed().forEach {
            g2d.drawImage(
                layerMap[it.type],
                it.x,
                it.y,
                it.width,
                it.height,
                null
            )
        }
        g2d.dispose()
        val os = ByteArrayOutputStream()
        ImageIO.write(image, "png", os)
        return ByteArrayInputStream(os.toByteArray())
    }

    data class DrawItem(
        val type: String,
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )

}