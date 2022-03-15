package cn.zeshawn.kaitobot.command.base

import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.session.base.Session
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.MessageChain

interface ChatCommand {
    val name: String
    val alias: List<String>
    val permission: UserRole

    suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain

    fun getHelp(): String

}

interface ConversationCommand {
    suspend fun handle(event: MessageEvent, session: Session, user: User)
}


interface CallbackCommand {
    // 此类命令执行完后需要回调
    fun callback(receipt: MessageReceipt<Contact>)
}

// 标记为命令不可用
annotation class Disabled

// 标记为命令将不在用户手册出现，但仍然可以调用
annotation class NotForUser