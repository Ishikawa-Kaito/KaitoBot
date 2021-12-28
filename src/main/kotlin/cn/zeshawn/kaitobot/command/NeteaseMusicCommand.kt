package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object NeteaseMusicCommand: ChatCommand {
    override val name: String
        get() = "网易云音乐命令"
    override val alias: List<String>
        get() = listOf("emo","163")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        TODO("Not yet implemented")
    }

    override fun getHelp(): String {
        TODO("Not yet implemented")
    }
}