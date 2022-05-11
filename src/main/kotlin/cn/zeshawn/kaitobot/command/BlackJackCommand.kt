package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.BlackjackService
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

object BlackJackCommand : ChatCommand {
    override val name: String
        get() = "二十一点游戏"
    override val alias: List<String>
        get() = listOf("bj")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        BlackjackService.start(event as GroupMessageEvent)
        return EmptyMessageChain
    }

    override fun getHelp(): String =
        "二十一点游戏，操作主要有/bj 下注 停牌 要牌。"
}