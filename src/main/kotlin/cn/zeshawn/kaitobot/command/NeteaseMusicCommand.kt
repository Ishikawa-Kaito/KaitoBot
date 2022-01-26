package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.NeteaseService
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MessageChain

object NeteaseMusicCommand : ChatCommand {
    override val name: String
        get() = "网抑云"
    override val alias: List<String>
        get() = listOf("emo", "163")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        return if (args.isEmpty()) {
            // 随机挑选一条emo
            try {
                NeteaseService.getRandomOne()
            } catch (_: Exception) {
                NeteaseService.getBackupOne()
            }

        } else if (args.size == 1 &&
            (args[0].contentEquals("?") || args[0].contentEquals("help"))
        ) {
            return getHelp().toChain()
        } else {//可能是英文歌，join各个参数
            val keyword = args.joinToString(separator = "")
            try {
                NeteaseService.getOne(keyword)
            } catch (_: Exception) {
                NeteaseService.getBackupOne()
            }
        }
    }

    override fun getHelp(): String = """
        /emo    [歌名]    按歌名emo
        /emo    直接emo
    """.trimIndent()
}