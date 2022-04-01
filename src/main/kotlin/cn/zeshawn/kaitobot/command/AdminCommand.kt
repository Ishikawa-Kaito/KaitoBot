package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.command.base.NotForUser
import cn.zeshawn.kaitobot.entity.Group
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.WordCloudService
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain


@NotForUser
object AdminCommand : ChatCommand {
    override val name: String
        get() = "管理员"
    override val alias: List<String>
        get() = listOf("a")
    override val permission: UserRole
        get() = UserRole.ADMIN

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        if (args.isEmpty()) {
            return EmptyMessageChain
        } else {
            when (args[0]) {
                "speak" -> {
                    val id = args[1].toLong()
                    event.bot.getGroup(id)?.sendMessage(args[2])
                }
                "achat" -> {
                    val id = args[1].toLong()
                    event.bot.getGroup(id)?.let {
                        Group.getGroupOrAdd(it.id).params["answer"] = args[2].toBoolean()
                    }
                }
                "wc" -> {
                    if (args.size < 3) {
                        val id = args[1].toLong()
                        val image = event.subject.uploadImage(WordCloudService.getWordCloud(id))
                        return buildMessageChain { +image }
                    }
                    val id = args[1].toLong()
                    event.bot.getGroup(id)?.let {
                        Group.getGroupOrAdd(it.id).params["word_cloud"] = args[2].toBoolean()
                    }
                }
            }
        }
        return EmptyMessageChain
    }

    override fun getHelp() = "You don't need help."
}