package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.WordCloudService
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain

object WordCloudCommand : ChatCommand {
    override val name: String
        get() = "群词云"
    override val alias: List<String>
        get() = listOf("wc")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        if (event.subject !is Group) return EmptyMessageChain
        val group = cn.zeshawn.kaitobot.entity.Group.getGroupOrAdd(event.subject.id)
        when (args.size) {
            0 -> {
                return if (group.params["word_cloud"] != true) {
                    "本群未开启词云统计功能，请管理员使用open命令开启。".toChain()
                } else {
                    val image = event.subject.uploadImage(WordCloudService.getWordCloud(group.id))
                    buildMessageChain { +image }
                }
            }
            1 -> {
                return when (args[0]) {
                    "open" -> {
                        if (
                            (event.subject as Group).members.getOrFail(event.sender.id).permission
                            > MemberPermission.MEMBER
                            ||
                            User.getUserOrRegister(event.sender.id).role > UserRole.ADMIN
                        ) {
                            group.params["word_cloud"] = true
                            "已开启词云统计。".toChain()
                        } else
                            EmptyMessageChain
                    }
                    "close" -> {
                        if (
                            (event.subject as Group).members.getOrFail(event.sender.id).permission
                            > MemberPermission.MEMBER
                            ||
                            User.getUserOrRegister(event.sender.id).role > UserRole.ADMIN
                        ) {
                            group.params["word_cloud"] = false
                            "已关闭词云统计。".toChain()
                        } else
                            EmptyMessageChain
                    }
                    else -> {
                        "无效参数".toChain()
                    }
                }
            }
            else -> {
                return "无效参数".toChain()
            }
        }

    }

    override fun getHelp(): String = """
        
    """.trimIndent()
}