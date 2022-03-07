package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.event.events.GroupMessageEvent
import java.time.Instant
import kotlin.random.Random

object ChitungListener : IListener {
    override val name: String
        get() = "七筒！！！"


    private val idiotLoveWords =
        listOf("七筒老婆！！！！！\uD83D\uDC8B\uD83D\uDC98\uD83D\uDC9D\uD83D\uDC8C\uD83D\uDE0D\uD83D\uDC93\uD83E\uDD1F\n我是你的舔狗啊！！！")

    @EventHandler
    fun sayHello(event: GroupMessageEvent) {
        if (event.message.contentToString().contains("七筒")) {
            if (Random(Instant.now().epochSecond).nextBoolean())
                runBlocking {
                    event.subject.sendMessage(idiotLoveWords.random())
                }
        }
    }
}