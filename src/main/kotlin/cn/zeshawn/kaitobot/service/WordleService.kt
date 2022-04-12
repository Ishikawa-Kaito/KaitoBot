package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.data.WordleData
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


object WordleService {

    init {
        WordleData.load()
        if (WordleData.file.exists() && WordleData.file.isDirectory){
            WordleData.file.listFiles()!!.forEach {
                if (it.extension.lowercase() == "png") {
                    ImageIO.read(it).let { image ->
                        val charMeta = it.name.chunked(1)
                        WordleData.charData[image] = Pair(charMeta[0],charMeta[1].toInt())
                    }
                }
            }
        }
    }

    fun getRows(respondImage: BufferedImage): MutableList<List<BufferedImage>>{
        val rows = mutableListOf<List<BufferedImage>>()
        val lines = buildList {
            for (i in 20..310 step 50) {
                val tempImg = respondImage.getSubimage(20,i,240,40)
                add(tempImg)
            }
        }
        lines.forEach {
            val words = buildList {
                for (i in 0..240 step 50) {
                    val word = it.getSubimage(i, 0, 40, 40)
                    add(word)
                }
            }
            rows.add(words)
        }
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

    fun compareByte(imgA:BufferedImage,imgB:BufferedImage):Boolean{
        for (i in 0 until imgA.width) {
            for (j in 0 until imgA.height) {
                if (imgA.getRGB(i,j)!=imgB.getRGB(i,j))
                    return false
            }
        }
        return true
    }


    private fun ocr(img:BufferedImage): Pair<String,Int> {
        WordleData.charData.keys.forEach {
            if (compareByte(it,img)){
                return WordleData.charData[it]!!
            }
        }
        return Pair("",0)
    }


    fun BufferedImage.toByteArray():ByteArray{
        val out = ByteArrayOutputStream()
        ImageIO.write(this,"PNG", out)
        return out.toByteArray()
    }


    fun reduceSize(image: BufferedImage, width: Int, height: Int): BufferedImage {
        var new_image: BufferedImage? = null
        val width_times = width.toDouble() / image.width
        val height_times = height.toDouble() / image.height
        new_image = if (image.type == BufferedImage.TYPE_CUSTOM) {
            val cm = image.colorModel
            val raster = cm.createCompatibleWritableRaster(width, height)
            val alphaPremultiplied = cm.isAlphaPremultiplied
            BufferedImage(cm, raster, alphaPremultiplied, null)
        } else {
            BufferedImage(width, height, image.type)
        }
        val g = new_image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.drawRenderedImage(image, AffineTransform.getScaleInstance(width_times, height_times))
        g.dispose()
        return new_image
    }

    /**
     * 得到灰度值
     * @param image
     * @return
     */
    fun getGrayValue(image: BufferedImage): Array<DoubleArray> {
        val width = image.width
        val height = image.height
        val pixels = Array(width) { DoubleArray(height) }
        for (i in 0 until width) {
            for (j in 0 until height) {
                pixels[i][j] = computeGrayValue(image.getRGB(i, j))
            }
        }
        return pixels
    }

    /**
     * 计算灰度值
     * @param pixels
     * @return
     */
    fun computeGrayValue(pixel: Int): Double {
        val red = pixel shr 16 and 0xFF
        val green = pixel shr 8 and 0xFF
        val blue = pixel and 255
        return 0.3 * red + 0.59 * green + 0.11 * blue
    }

    fun avgImage(smallImage: Array<IntArray>): Int {
        var avg = -1
        var sum = 0
        var count = 0
        for (i in smallImage.indices) {
            for (j in smallImage[i].indices) {
                sum += smallImage[i][j]
                count++
            }
        }
        avg = sum / count
        return avg
    }

    fun to64(avg: Int, smallImage: Array<IntArray>): String? {
        var result = ""
        for (i in smallImage.indices) {
            for (j in smallImage[i].indices) {
                result += if (smallImage[i][j] > avg) {
                    "1"
                } else {
                    "0"
                }
            }
        }
        return result
    }

    //越小越相似
    fun compareFingerPrint(orgin_fingerprint: String, compared_fingerprint: String): Int {
        var count = 0
        for (i in 0 until orgin_fingerprint.length) {
            if (orgin_fingerprint[i] != compared_fingerprint[i]) {
                count++
            }
        }
        return count
    }

}
