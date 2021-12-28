package cn.zeshawn.kaitobot.util

import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.toMessageChain


fun String.toChain(): MessageChain {
    return PlainText(this).toMessageChain()
}