package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.util.FileUtil.newFile
import cn.zeshawn.kaitobot.util.getUrlStream
import com.squareup.gifencoder.*
import net.mamoe.mirai.event.events.NudgeEvent
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.roundToInt


object PetGifService {

    private const val OUT_SIZE = 112
    private const val MAX_FRAME = 5

    private const val squish = 1.25
    private const val scale = 0.875
    private const val spriteY = 20.0
    private const val duration = 16


    private val hands: Array<BufferedImage> by lazy {
        val hand = ImageIO.read(
            File("petpet.png")
        )
        Array(5) {
            hand.getSubimage(it * 112, 0, 112, 112)
        }
    }
    private val frameOffsets = listOf(
        mapOf("x" to 0, "y" to 0, "w" to 0, "h" to 0),
        mapOf("x" to -4, "y" to 12, "w" to 4, "h" to -12),
        mapOf("x" to -12, "y" to 18, "w" to 12, "h" to -18),
        mapOf("x" to -8, "y" to 12, "w" to 4, "h" to -12),
        mapOf("x" to -4, "y" to 0, "w" to 0, "h" to 0)
    )

    suspend fun getPetGif(event: NudgeEvent): File {
        val outputFile = newFile("${event.from.id}-petpet.gif")
        val avatar = ImageIO.read(event.from.avatarUrl.getUrlStream())
        val outputStream = FileOutputStream(outputFile)
        val encoder = GifEncoder(outputStream, 112, 112, 0)
        val option = ImageOptions()
            .setColorQuantizer(MedianCutQuantizer.INSTANCE)
            .setDitherer(FloydSteinbergDitherer.INSTANCE)
            .setDisposalMethod(DisposalMethod.DO_NOT_DISPOSE)
        val delayCentiSeconds = option::class.java.getDeclaredField("delayCentiseconds")
        delayCentiSeconds.isAccessible = true
        delayCentiSeconds.set(option, (100.0F / 15).roundToInt())
        for (i in 0 until MAX_FRAME) {
            val rgb = generateFrame(avatar, i).getRGB(0, 0, 112, 112, IntArray(112 * 112), 0, 112)
            val frame = Image.fromRgb(rgb, 112)
            encoder.addImage(frame, option)
        }
        encoder.finishEncoding()
        outputStream.close()
        return outputFile
    }


    private fun getSpriteFrame(i: Int): Map<String, Int> {
        val offset = frameOffsets[i]
        return mapOf(
            "dx" to ((offset - "x") * squish * 0.4).toInt(),
            "dy" to (spriteY + (offset - "y") * squish * 0.9).toInt(),
            "dw" to ((OUT_SIZE + (offset - "w") * squish) * scale).toInt(),
            "dh" to ((OUT_SIZE + (offset - "h") * squish) * scale).toInt()
        )
    }

    private fun generateFrame(head: BufferedImage, i: Int): BufferedImage {
        val cf = getSpriteFrame(i)
        val result = BufferedImage(OUT_SIZE, OUT_SIZE, BufferedImage.TYPE_INT_ARGB)
        result.createGraphics().apply {
            create().apply {
                translate(cf - "dx" + 15, cf - "dy" + 15)
                drawImage(head, 0, 0, ((cf - "dw") * 0.9).toInt(), ((cf - "dh") * 0.9).toInt(), null)
            }
            drawImage(hands[i], 0, max(0.0, ((cf - "dy") * 0.75 - max(0.0, spriteY) - 0.5)).toInt(), null, null)
        }
        return result
    }

}

operator fun <K, V> Map<K, V>.minus(x: K): V {
    return getValue(x)
}


