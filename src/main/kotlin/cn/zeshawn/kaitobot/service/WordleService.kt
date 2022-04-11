package cn.zeshawn.kaitobot.service

import net.sourceforge.tess4j.ITesseract
import net.sourceforge.tess4j.Tesseract
import org.bytedeco.javacv.Java2DFrameUtils
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Rect
import java.awt.image.BufferedImage
import java.io.File
import java.time.Instant
import javax.imageio.ImageIO


object WordleService {
    fun getWords(respondImage:BufferedImage){
        val tess: ITesseract = Tesseract()
        val mat = Java2DFrameUtils.toMat(respondImage)
        val lines = buildList {
            for (i in 20..310 step 50){
                val tempMat = Mat(mat, Rect(20,i,240,40))
                add(tempMat)
            }
        }
        var i = 0
        lines.forEach {
            val a = Java2DFrameUtils.toBufferedImage(it)
            val outputfile = File("F:\\${i++}.jpg")
            ImageIO.write(a, "jpg", outputfile);
        }
    }

}