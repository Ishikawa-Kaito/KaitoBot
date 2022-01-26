package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.MemeService
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain

object MemeCommand : ChatCommand {
    override val name: String
        get() = "外网meme"
    override val alias: List<String>
        get() = listOf("memes", "梗图")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        return if (args.size == 1 && args[0] in listOf("?", "help")) {
            getHelp().toChain()
        } else {
            event.subject.sendMessage("正在获取meme>>>>>>>")
            val memeImg = MemeService.getRandomMeme()
            if (memeImg != null) {
                val image = event.subject.uploadImage(memeImg)
                buildMessageChain {
                    +image
                }
            } else {
                "出现了一点小问题.".toChain()
            }
        }
    }

    override fun getHelp(): String {
        return """
            /meme 获得一张英文的梗图，也许你看不懂，我也看不懂，但是是梗图。
        """.trimIndent()
    }
}