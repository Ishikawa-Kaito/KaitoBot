package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.AbstractWordService
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object AbstractWordCommand : ChatCommand {
    override val name: String
        get() = "抽象话转换"
    override val alias: List<String>
        get() = listOf("cx", "抽象")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        return AbstractWordService.getAbstract(args.joinToString("")).toChain()
    }

    override fun getHelp() = """
        /cx [待转换内容]
        突发奇想的一个无聊小功能
    """.trimIndent()
}