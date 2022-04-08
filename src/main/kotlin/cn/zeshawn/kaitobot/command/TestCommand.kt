package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.command.base.NotForUser
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.OreoService
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain

@NotForUser
object TestCommand : ChatCommand {
    override val name: String
        get() = "测试"
    override val alias: List<String>
        get() = listOf("test", "debug")
    override val permission: UserRole
        get() = UserRole.ADMIN

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        val oreo = args[0]
        val imageStream = OreoService.generateOreo(oreo) ?: return EmptyMessageChain
        val image = event.subject.uploadImage(imageStream = imageStream)
        return buildMessageChain {
            +image
        }
    }

    override fun getHelp(): String = """
        /test
    """.trimIndent()

}