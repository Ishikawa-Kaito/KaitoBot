package cn.zeshawn.kaitobot.util

import cn.hutool.http.HttpUtil
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.ImageIO


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


fun String.getUrlStream(): InputStream = ByteArrayInputStream(HttpUtil.downloadBytes(this))