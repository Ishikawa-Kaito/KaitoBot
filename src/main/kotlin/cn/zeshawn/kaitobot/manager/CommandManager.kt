package cn.zeshawn.kaitobot.manager

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.command.base.ChatCommand
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder

object CommandManager {

    private val commands: MutableSet<ChatCommand> = mutableSetOf()

    // 注册一条命令
    private fun registerCommand(cmd: ChatCommand) {
        if (!commands.add(cmd)) {
            KaitoMind.KaitoLogger.warn("[Command] 正在尝试注册已有命令 ${cmd.name}")
        } else {
            KaitoMind.KaitoLogger.info("[Command] 注册命令 ${cmd.name}")
        }
    }

    // 注册很多条命令
    private fun registerCommands(commands: Array<ChatCommand>) {
        commands.forEach {
            registerCommand(it)
        }
    }

    fun autoSetup(packageName: String) {
        KaitoMind.KaitoLogger.info("[Command] 开始自动注册命令，命令包名${packageName}")
        val reflection = Reflections(ConfigurationBuilder().forPackage(packageName))
        val commands = reflection.getSubTypesOf(ChatCommand::class.java)
            .map { it.kotlin }
            .mapNotNull { it.objectInstance }
            .toTypedArray()
        this.registerCommands(commands)
    }

    fun getCommand(cmdName: String): ChatCommand? {
        val command = commands.parallelStream().filter {
            canYouCallMeByThis(it, cmdName)
        }.findFirst()
        return if (command.isPresent) command.get() else null
    }

    fun getCommandName(message: String): String {
        val cmdPrefix = getCommandPrefix(message)
        if (cmdPrefix == "") {
            return message.split(" ")[0]
        }
        val index = message.indexOf(cmdPrefix) + cmdPrefix.length
        return message.substring(index, message.length).split(" ")[0]
    }

    fun getCommandPrefix(message: String): String {
        if (message.isNotEmpty()) {
            KaitoMind.config.commandPrefix.forEach {
                if (message.startsWith(it)) {
                    return it
                }
            }
        }
        return ""
    }


    private fun canYouCallMeByThis(cmd: ChatCommand, tryWord: String): Boolean {
        return when {
            tryWord == cmd.name -> true
            cmd.alias.isNotEmpty() -> cmd.alias.parallelStream()
                .filter {
                    it!!.contentEquals(tryWord)
                }.findFirst().isPresent
            else -> false
        }
    }

    fun getAllCommand(): List<ChatCommand> {
        return commands.toList()
    }
}