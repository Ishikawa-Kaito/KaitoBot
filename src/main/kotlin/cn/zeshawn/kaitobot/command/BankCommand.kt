package cn.zeshawn.kaitobot.command

import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.UserRole
import cn.zeshawn.kaitobot.service.BankService
import cn.zeshawn.kaitobot.util.toChain
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.buildMessageChain

object BankCommand : ChatCommand {
    override val name: String
        get() = "银行"
    override val alias: List<String>
        get() = listOf("bk", "余额")
    override val permission: UserRole
        get() = UserRole.USER

    override suspend fun execute(event: MessageEvent, args: List<String>, user: User): MessageChain {
        if (args.isNotEmpty()) {
            if (args[0] == "rank") {
                if (event is GroupMessageEvent) {
                    val rank = BankService.getDCoinRank(event.subject.members)
                    return buildMessageChain {
                        +"本群前五排行：\n"
                        rank.forEach {
                            +event.subject.members[it.first]!!.nick
                            +" : ${it.second}\n"
                        }
                    }
                }

            }
            if (user.role > UserRole.USER) {
                when (args.size) {
                    1 -> BankService.addDCoin(event.sender.id, args[0].toLong())
                    2 -> BankService.addDCoin(args[0].replace("@", "").toLong(), args[1].toLong())
                }
                return EmptyMessageChain
            }
        }
        return "你的名下共有 ${BankService.getDCoin(event.sender.id)} 斗币！真有钱。".toChain()
    }

    override fun getHelp(): String {
        return "那就是查账呗不就是。"
    }
}