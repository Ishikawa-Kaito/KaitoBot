package cn.zeshawn.kaitobot.util

import cn.hutool.http.HttpUtil
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.Java2DFrameUtils
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Scalar
import org.bytedeco.opencv.opencv_core.Size
import java.awt.image.BufferedImage
import java.awt.image.PixelGrabber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min


fun BufferedImage.toInputStream(): InputStream {
    val os = ByteArrayOutputStream()
    ImageIO.write(this, "png", os)
    return ByteArrayInputStream(os.toByteArray())
}

fun BufferedImage.hash(): String {
    return this.getRGB(
        0, 0, this.width, this.height,
        null, 0, this.width
    ).contentHashCode().toString()
}

fun BufferedImage.toMat(): Mat {
    return OpenCVFrameConverter.ToMat().convertToMat(Java2DFrameConverter().convert(this)).clone()
}

fun Mat.toBufferedImage(): BufferedImage {
    return Java2DFrameUtils.deepCopy(
        Java2DFrameConverter().getBufferedImage(
            OpenCVFrameConverter.ToMat().convert(this).clone()
        )
    )
}

fun String.getUrlStream(): InputStream = ByteArrayInputStream(HttpUtil.downloadBytes(this))

fun BufferedImage.imageResize(inputSize: Int): Array<Array<Array<FloatArray>>> {
    val width = this.width
    val height = this.height
    val ratio = min(inputSize * 1.0 / max(width, height), 1.0)
    val imgMat = this.toMat()
    val resized = Mat()
    opencv_imgproc.resize(
        imgMat,
        resized,
        Size((width * ratio).toInt(), (height * ratio).toInt()),
        0.0,
        0.0,
        opencv_imgproc.INTER_LINEAR
    )
    val padWidth = inputSize - resized.size(1)
    val padHeight = inputSize - resized.size(0)
    val dstMat = Mat(inputSize, inputSize)
    opencv_core.copyMakeBorder(
        resized, dstMat, padHeight / 2 + (padHeight % 2), padHeight / 2, padWidth / 2 + (padWidth % 2), padWidth / 2,
        opencv_core.BORDER_CONSTANT,
        Scalar(0.0, 0.0, 0.0, 0.0)
    )
    val resizedImage = dstMat.toBufferedImage()
    val pixels = IntArray(inputSize * inputSize)
    val pg = PixelGrabber(resizedImage, 0, 0, inputSize, inputSize, pixels, 0, inputSize)
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
    while (row * inputSize + col < pixels.size) {
        pixel = row * inputSize + col
        ret[0][2][row][col] = (pixels[pixel] and 0x000000FF) / 255f // blue
        ret[0][1][row][col] = (pixels[pixel] shr 8 and 0x000000FF) / 255f // green
        ret[0][0][row][col] = (pixels[pixel] shr 16 and 0x000000FF) / 255f // red
        col++
        if (col == inputSize - 1) {
            col = 0
            row++
        }
    }
    return ret
}