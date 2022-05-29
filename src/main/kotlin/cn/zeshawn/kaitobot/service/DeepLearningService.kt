package cn.zeshawn.kaitobot.service

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import cn.zeshawn.kaitobot.util.imageResize
import cn.zeshawn.kaitobot.util.toBufferedImage
import cn.zeshawn.kaitobot.util.toMat
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Point
import org.bytedeco.opencv.opencv_core.Scalar
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min

object DeepLearningService {
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()
    private val HaoSession: OrtSession =
        env.createSession(
            "C:\\Users\\63086\\IdeaProjects\\KaitoBot\\build\\classes\\kotlin\\main\\data\\hao.onnx",
            OrtSession.SessionOptions()
        )


    fun drawRect(img: BufferedImage, boxes: List<FloatArray>): BufferedImage {
        val width = img.width
        val height = img.height
        val ratio = min(640 * 1.0 / max(width, height), 1.0)
        val imgMat = img.toMat()
        val padWidth = (640 - (width * ratio).toInt())
        val padHeight = (640 - (height * ratio).toInt())
        boxes.forEach {
            val xyxy = xywh2xyxy(
                listOf(
                    ((it[0] - padWidth / 2) / ratio).toInt(),
                    ((it[1] - padHeight / 2) / ratio).toInt(),
                    (it[2] / ratio).toInt(),
                    (it[3] / ratio).toInt()
                )
            )
            opencv_imgproc.rectangle(
                imgMat,
                xyxy[0],
                xyxy[1],
                Scalar(0.0, 0.0, 255.0, 0.0),
                width / 200,
                opencv_imgproc.LINE_8,
                0
            )
        }
        return imgMat.toBufferedImage()
    }


    fun detectHaoHao(img: BufferedImage): List<FloatArray> {
        val features = img.imageResize(640)
        val tensor = OnnxTensor.createTensor(env, features)
        val inputs = mapOf<String, OnnxTensor>("images" to tensor)
        val result = HaoSession.run(inputs, setOf("output"))

        @Suppress("UNCHECKED_CAST")
        val boxes = ((result[0].value as Array<*>)[0] as Array<FloatArray>).filter { it[4] > 0.5 } // 筛选置信度大于0.5的框
        if (boxes.isEmpty()) {
            return emptyList()
        }
        return nms(boxes)
//        val mat = img.toMat()
//        resultBoxes.forEach {
//            val xyxy = xywh2xyxy(listOf(it[0].toInt(),it[1].toInt(),it[2].toInt(),it[3].toInt()))
//            opencv_imgproc.rectangle(mat,xyxy[0],xyxy[1],Scalar(0.0,0.0,255.0,0.0))
//        }
//
//        mat.toBufferedImage().Save("test.png")
    }


    fun nms(boxes: List<FloatArray>): List<FloatArray> {
        val sortedBoxes = boxes.sortedBy { -it[4] }
        val tempBoxes = sortedBoxes.toMutableList()
        val resultBoxes = mutableListOf<FloatArray>()
        while (tempBoxes.isNotEmpty()) {
            val candidateBox = tempBoxes.first()
            resultBoxes.add(candidateBox)
            tempBoxes.remove(candidateBox)
            tempBoxes.removeAll { iou(candidateBox, it) > 0.45 }
        }
        return resultBoxes
    }

    private fun iou(box1: FloatArray, box2: FloatArray): Double {
        val area1 = box1[2] * box1[3]
        val area2 = box2[2] * box2[3]
        val xyxyOfBox1 = xywh2xyxy(listOf(box1[0].toInt(), box1[1].toInt(), box1[2].toInt(), box1[3].toInt()))
        val xyxyOfBox2 = xywh2xyxy(listOf(box2[0].toInt(), box2[1].toInt(), box2[2].toInt(), box2[3].toInt()))
        val x1 = max(xyxyOfBox1[0].x(), xyxyOfBox2[0].x())
        val y1 = max(xyxyOfBox1[0].y(), xyxyOfBox2[0].y())
        val x2 = min(xyxyOfBox1[1].x(), xyxyOfBox2[1].x())
        val y2 = min(xyxyOfBox1[1].y(), xyxyOfBox2[1].y())
        val intersectionArea = max(x2 - x1 + 1.0, 0.0) * max(y2 - y1 + 1.0, 0.0)
        return intersectionArea / (area1 + area2 - intersectionArea)
    }


    fun xywh2xyxy(xywh: List<Int>): List<Point> {
        val x = xywh[0]
        val y = xywh[1]
        val w = xywh[2]
        val h = xywh[3]
        return listOf(
            Point(
                x - w / 2,
                y - h / 2
            ),
            Point(
                x + w / 2,
                y + w / 2
            )
        )
    }

}