package cn.zeshawn.kaitobot.command.base

import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

interface ChatCommand {
    val name: String
    val alias: List<String>
    val permission: UserRole

    suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain

    fun getHelp(): String

}

interface ConversationCommand {
    // 对话命令，暂时没空写
}



