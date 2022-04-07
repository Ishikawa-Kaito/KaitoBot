package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.ShadiaoService
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object CPCommand : ChatCommand {
    override val name: String
        get() = "一般路过同人女"
    override val alias: List<String>
        get() = listOf("cp", "磕")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        return when (args.size) {
            0 -> {
                ShadiaoService.getSweetNothings().toChain()
            }
            1 -> {
                val split = args[0].length / 2
                ShadiaoService.getCPStory(args[0].substring(0, split), args[0].substring(split, args[0].length))
                    .toChain()
            }
            2 -> {
                ShadiaoService.getCPStory(args[0], args[1]).toChain()
            }
            else -> {
                execute(event, args.subList(0, 2), user)
            }
        }
    }

    override fun getHelp() = """
        /cp     来一条渣男语录
        /cp [任意名字]  精神分裂
        /cp [攻] [受]  同人女
    """.trimIndent()
}