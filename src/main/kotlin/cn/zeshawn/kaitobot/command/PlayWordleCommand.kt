package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.nextEventAsync
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

object PlayWordleCommand:ChatCommand {
    override val name: String
        get() = "玩猜单词"
    override val alias: List<String>
        get() = listOf("kwordle","kw")
    override val permission: UserRole
        get() = UserRole.DEV

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        if (event !is GroupMessageEvent) return EmptyMessageChain
        if (!event.subject.members.contains(3270864281)) return EmptyMessageChain
        event.subject.sendMessage("/wordle")
        return EmptyMessageChain
    }

    override fun getHelp(): String {
        TODO("Not yet implemented")
    }


}