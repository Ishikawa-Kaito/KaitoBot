package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.KaitoMind
import org.bytedeco.javacv.Java2DFrameUtils
import org.bytedeco.javacv.LeptonicaFrameConverter
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.opencv_core.Rect
import org.bytedeco.tesseract.TessBaseAPI
import org.bytedeco.tesseract.Tesseract
import java.awt.image.BufferedImage


object WordleService {
    val tess = TessBaseAPI().apply {
        this.Init("${KaitoMind.root}/data/traindata","eng")
    }



    fun getRows(respondImage: BufferedImage): MutableList<List<Mat>>{

        val mat = Java2DFrameUtils.toMat(respondImage)
        val rows = mutableListOf<List<Mat>>()
        val lines = buildList {
            for (i in 20..310 step 50) {
                val tempMat = Mat(mat, Rect(20, i, 240, 40))
                add(tempMat)
            }
        }
        lines.forEach {
            val words = buildList {
                for (i in 0..240 step 50) {
                    val word = Mat(it, Rect(i, 0, 40, 40))
                    add(word)
                }
            }
            rows.add(words)
        }
//        var i = 0
//        rows.forEach { row ->
//            row.forEach {
//                val a = Java2DFrameUtils.toBufferedImage(it)
//                val outputfile = File("C:\\Users\\63086\\Desktop\\wordle\\${i++}.jpg")
//                ImageIO.write(a, "jpg", outputfile);
//            }
//        }

        return rows
    }

    fun solve(respondImage: BufferedImage){
        val rows = getRows(respondImage)
        rows.forEach {
            it.forEach { mat ->
                println(ocr(mat))
            }
        }

    }


    private fun ocr(m: Mat): String {
        val mat = m.clone()
        val converterA = OpenCVFrameConverter.ToIplImage()
        val converterB = LeptonicaFrameConverter()
        val pix = converterB.convert(converterA.convert(mat))
        tess.SetImage(pix)
        return tess.GetUTF8Text().string
    }

}