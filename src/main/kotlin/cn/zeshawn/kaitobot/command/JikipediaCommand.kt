package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.JikipediaService
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain

object JikipediaCommand : ChatCommand {
    override val name: String
        get() = "小鸡词典"
    override val alias: List<String>
        get() = listOf("jk", "geng", "什么梗")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        if (args.isEmpty()) {
            return EmptyMessageChain
        }
        val id = JikipediaService.getId(args.joinToString(""))
        val res = JikipediaService.getDefinition(id)
        return buildMessageChain {
            +res.first
            res.second.forEach {
                val image = event.subject.uploadImage(it)
                +image
            }
        }
    }

    override fun getHelp(): String = """
        可以搜索一些梗的意思，数据来源小鸡词典。
        使用方法：
        /geng [搜索内容]
    """.trimIndent()
}