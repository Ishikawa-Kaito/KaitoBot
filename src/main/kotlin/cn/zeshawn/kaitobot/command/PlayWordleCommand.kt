package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

object PlayWordleCommand : ChatCommand {
    override val name: String
        get() = "自动玩猜单词"
    override val alias: List<String>
        get() = listOf("auto_wordle", "aw")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        if (event.subject !is Group) return EmptyMessageChain
        val group = cn.zeshawn.kaitobot.entity.Group.getGroupOrAdd(event.subject.id)
        if ((event.subject as Group).members.getOrFail(event.sender.id).permission
            < MemberPermission.ADMINISTRATOR &&
            User.getUserOrRegister(event.sender.id).role < UserRole.ADMIN
        ) {
            return EmptyMessageChain
        }

        return if (args.isEmpty()) {
            if (group.params["auto_wordle"] == true) {
                group.params["auto_wordle"] = false
                "自动wordle已关闭。".toChain()
            } else {
                group.params["auto_wordle"] = true
                "自动wordle已打开。".toChain()
            }
        } else if (args.size == 1) {
            when (args[0]) {
                "open", "on" -> {
                    group.params["auto_wordle"] = true
                    "自动wordle已打开。".toChain()
                }
                "close", "off" -> {
                    group.params["auto_wordle"] = false
                    "自动wordle已关闭。".toChain()
                }
                else -> EmptyMessageChain
            }
        } else {
            EmptyMessageChain
        }

    }

    override fun getHelp() = """
        /aw on 开启自动wordle
        /aw off 关闭自动wordle
        仅管理员调用有效
        开启后会自动根据nullbot生成的wordle图片发送一个最优解，延时为1000ms。
    """.trimIndent()


}