package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.EmojiMixService
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.toMessageChain

object EmojiMixCommand : ChatCommand {
    override val name: String
        get() = "emoji合成"
    override val alias: List<String>
        get() = listOf("mix")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        return when (args.size) {
            2 -> {
                val e1 = args[0]
                val e2 = args[1]
                val file = EmojiMixService.getEmojiMix(e1, e2)
                if (file == null) "找不到对应的合成。".toChain()
                else event.subject.uploadImage(file).toMessageChain()
            }
            else ->
                EmptyMessageChain
        }
    }

    override fun getHelp(): String = """
        /mix [emoji_1] [emoji_2]
        所有资源来自Google。
    """.trimIndent()
}