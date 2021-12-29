package cn.zeshawn.kaitobot.manager

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.command.base.ChatCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.entity.hasPermission
import cn.zeshawn.kaitobot.util.LogUtil
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.event.subscribeMessages
import net.mamoe.mirai.message.data.EmptyMessageChain
import net.mamoe.mirai.message.data.MessageChain

object MessageManager {

    fun register(bot: Bot) {
        bot.eventChannel.subscribeMessages {
            always {
                try {
                    val res = callCommand(this)

                    if (res.status.isOk()) {
                        this.subject.sendMessage(res.reply)
                    }
                    // 返回命令执行结果

                } catch (e: Exception) {
                    KaitoMind.KaitoLogger.warn("[Command] 尝试执行命令时发生错误")
                    KaitoMind.KaitoLogger.warn(LogUtil.formatStacktrace(e, null, true))
                }
            }
        }
    }


    private suspend fun callCommand(event: MessageEvent): CommandResult {
        val message = event.message.contentToString()
        val cmdName = CommandManager.getCommandName(message)
        // 消息中是否包含命令
        val cmd = CommandManager.getCommand(cmdName) ?: return CommandResult(EmptyMessageChain, null)

        val user = User.getUserOrRegister(event.sender.id)
        if (!user.hasPermission(cmd)) {//权限不足，跳出
            return CommandResult(EmptyMessageChain, cmd, CommandStatus.NoPermission())
        }

        val prefix = CommandManager.getCommandPrefix(message)
        return if (prefix.isNotEmpty()) {
            // 前缀末尾下标
            val index = message.indexOf(prefix) + prefix.length

            val dePrefixMessage = message.substring(index, message.length).trim().split(" ")
            val argsList = dePrefixMessage.subList(1, dePrefixMessage.size)

            KaitoMind.KaitoLogger.debug("[命令] ${user.userId} 尝试执行命令: ${cmd.name} (原始消息: ${dePrefixMessage}, 解析参数: ${argsList})")

            val result = cmd.execute(event, argsList, user)
            CommandResult(result, cmd, CommandStatus.Success())
        } else {
            CommandResult(EmptyMessageChain, cmd, CommandStatus.NotACommand())
        }
    }
}

data class CommandResult(
    val reply: MessageChain,
    val cmd: ChatCommand?,
    val status: CommandStatus = CommandStatus.Failed()
)

sealed class CommandStatus(val name: String, private val isSuccessful: Boolean) {
    class Success : CommandStatus("成功", true)
    class NoPermission : CommandStatus("无权限", false)
    class Failed : CommandStatus("失败", false)
    class NotACommand : CommandStatus("不是命令", false)

    fun isOk(): Boolean = this.isSuccessful
}