package cn.zeshawn.kaitobot.util

import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toMessageChain
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toKotlinDuration


fun String.toChain(): MessageChain {
    return PlainText(this).toMessageChain()
}

fun LocalDateTime.getLastingTime(): Duration {
    return java.time.Duration.between(this, LocalDateTime.now()).toKotlinDuration()
}

fun LocalDateTime.getLastingTimeAsString(unit: TimeUnit = TimeUnit.SECONDS, msMode: Boolean = false): String {
    val duration = getLastingTime()
    return duration.toFriendly(maxUnit = unit, msMode = msMode)
}

fun Duration.toFriendly(maxUnit: TimeUnit = TimeUnit.DAYS, msMode: Boolean = true): String {
    val days = toInt(DurationUnit.DAYS)
    val hours = toInt(DurationUnit.HOURS) % 24
    val minutes = toInt(DurationUnit.MINUTES) % 60
    val seconds = (toInt(DurationUnit.SECONDS) % 60 * 1000) / 1000
    val ms = (toInt(DurationUnit.MILLISECONDS) % 60 * 1000 * 1000) / 1000 / 1000
    return buildString {
        if (days != 0 && maxUnit >= TimeUnit.DAYS) append("${days}天")
        if (hours != 0 && maxUnit >= TimeUnit.HOURS) append("${hours}时")
        if (minutes != 0 && maxUnit >= TimeUnit.MINUTES) append("${minutes}分")
        if (seconds != 0 && maxUnit >= TimeUnit.SECONDS) append("${seconds}秒")
        if (maxUnit >= TimeUnit.MILLISECONDS && msMode) append("${ms}毫秒")
    }
}

fun String.isNumeric(): Boolean {
    return matches("[-+]?\\d*\\.?\\d+".toRegex()) && !this.contains(".")
}


fun String.isAlphabet(): Boolean {
    val regex = Regex("[a-zA-Z]+?")
    return regex.matches(this)
}

fun String.getChineseLength(): Int {
    var length = 0
    for (i in this.indices) {
        val ascii = Character.codePointAt(this, i)
        if (ascii in 0..255) length++ else length += 2
    }
    return length
}

fun String.isChinese(): Boolean {
    val regEx = "[\\u4e00-\\u9fa5]+"
    val pattern: Pattern = Pattern.compile(regEx)
    return pattern.matcher(this).matches()
}

