package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain


object TestCommand : ChatCommand {
    override val name: String
        get() = "测试"
    override val alias: List<String>
        get() = listOf("test", "debug")
    override val permission: UserRole
        get() = UserRole.ADMIN

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
//        val a = PetGifService.getPetGif(event)
//        val img = event.subject.uploadImage(a)
//        return buildMessageChain {
//            +img
//        }
        return EmptyMessageChain
    }

    override fun getHelp(): String = """
        /test
    """.trimIndent()

}