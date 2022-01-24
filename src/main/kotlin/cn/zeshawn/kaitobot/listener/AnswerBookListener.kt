package cn.zeshawn.kaitobot.listener


import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.service.TheAnswerBookService
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.At

object AnswerBookListener : IListener {
    override val name: String
        get() = "随机回答"


    @EventHandler
    fun randomAnswer(event: MessageEvent) {
        val message = event.message
        val reg = ".*教.*|.*([吗呢吧])|.*(建议|意见|选择|怎|如何|要不要|该不该|能不能|可不可|行不行).*".toRegex()
        if (reg.containsMatchIn(message.contentToString()) && message.any{it is At && it.target==event.bot.id}) {

            runBlocking {
                event.subject.sendMessage(TheAnswerBookService.getAnswer())
            }
        }
    }
}