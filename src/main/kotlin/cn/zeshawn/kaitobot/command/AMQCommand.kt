package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.ShadiaoService
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object AMQCommand : ChatCommand {
    override val name: String
        get() = "鸡汤有毒"
    override val alias: List<String>
        get() = listOf("du", "毒鸡汤", "djt")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        return ShadiaoService.getAntiMotivationalQuote().toChain()
    }

    override fun getHelp() = """
        /du 问渠那得清如许，唯有毒汤活水来！
        /毒鸡汤 生活不止眼前的苟且，还有就是要打中文全称的你。
        /djt lywdjt
    """.trimIndent()
}