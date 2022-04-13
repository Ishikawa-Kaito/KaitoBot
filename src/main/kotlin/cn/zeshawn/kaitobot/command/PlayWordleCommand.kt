package cn.zeshawn.kaitobot.command

import cn.hutool.http.HttpUtil
import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.WordleService
import cn.zeshawn.kaitobot.service.getUrlStream
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.nextEventAsync
import net.mamoe.mirai.event.subscribeGroupMessages
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import javax.imageio.ImageIO

object PlayWordleCommand:ChatCommand {
    override val name: String
        get() = "玩猜单词"
    override val alias: List<String>
        get() = listOf("kwordle","kw")
    override val permission: UserRole
        get() = UserRole.DEV

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        if (event !is GroupMessageEvent) return EmptyMessageChain
        val message = event.message
        if (message.contains(Image)){
            val image = message.first { it is Image } as Image
            return WordleService.solve(image.toMessageChain(),event.subject.id).toChain()
        }
        return EmptyMessageChain
    }

    override fun getHelp(): String {
        TODO("Not yet implemented")
    }


}