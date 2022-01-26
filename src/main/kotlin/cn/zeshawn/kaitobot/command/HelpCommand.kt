package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.manager.CommandManager
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain

object HelpCommand : ChatCommand{
    override val name: String
        get() = "帮助"
    override val alias: List<String>
        get() = listOf("help","?","？")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        return if (args.isEmpty()) {
            buildMessageChain {
                var i = 1
                CommandManager.getAllCommand().forEach {
                    +PlainText("${i++}.${it.name}\n调用：${it.alias.joinToString(", ")}\n")
                }
                +PlainText("输入/? [命令] 可以获得命令的详细帮助。")
            }
        } else{
            val arg = args.joinToString("")
            CommandManager.getCommand(arg)?.getHelp()?.toChain() ?: EmptyMessageChain
        }
    }

    override fun getHelp() = ""
}