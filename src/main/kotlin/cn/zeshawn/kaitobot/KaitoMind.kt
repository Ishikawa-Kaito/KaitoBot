package cn.zeshawn.kaitobot

import cn.hutool.core.io.resource.ResourceUtil
import cn.zeshawn.kaitobot.core.Kaito
import cn.zeshawn.kaitobot.entity.Config
import cn.zeshawn.kaitobot.entity.Group
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.util.FileUtil
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import mu.KotlinLogging
import java.awt.Font
import java.awt.Font.TRUETYPE_FONT
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap

// 存点全局变量
object KaitoMind {

    //机器人实例
    val kaito: Kaito = Kaito()

    val debug: Boolean = System.getProperty("debug", "false").toBoolean()

    //配置设置
    internal lateinit var config: Config

    //根目录
    internal val root: File by lazy {
        FileUtil.getJarLocation()
    }

    //日志
    val KaitoLogger = KotlinLogging.logger("KaitoApp")

    //全部用户
    internal var users: MutableMap<Long, User> = ConcurrentHashMap()

    //全部群设置
    internal var groups: MutableMap<Long, Group> = ConcurrentHashMap()

    //一些网易云评论
    internal val music163Comments = mutableListOf<String>()


    internal val GenYoMinFont = Font.createFont(TRUETYPE_FONT, ResourceUtil.getStream("fonts/GenYoMin-B.ttc"))
    internal val InconsolataFont = Font.createFont(TRUETYPE_FONT, ResourceUtil.getStream("fonts/Inconsolata-VF.ttf"))

    //Jackson mapper
    val mapper: ObjectMapper = ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .registerModules(
            JavaTimeModule().also {
                it.addSerializer(
                    LocalDateTime::class.java,
                    LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"))
                )
                it.addSerializer(LocalDate::class.java, LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy/MM/dd")))
                it.addSerializer(LocalTime::class.java, LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")))
                it.addDeserializer(
                    LocalDate::class.java,
                    LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                )
                it.addDeserializer(
                    LocalTime::class.java,
                    LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss"))
                )

            },
            KotlinModule.Builder().enable(KotlinFeature.NullIsSameAsDefault)
                .enable(KotlinFeature.NullToEmptyCollection)
                .enable(KotlinFeature.NullToEmptyMap).build()

        )
        .setDateFormat(SimpleDateFormat("yyyy/MM/dd HH:mm:ss"))
}