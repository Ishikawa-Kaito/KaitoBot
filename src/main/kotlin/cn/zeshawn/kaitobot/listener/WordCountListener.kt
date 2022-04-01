package cn.zeshawn.kaitobot.listener

import cn.zeshawn.kaitobot.entity.Group
import cn.zeshawn.kaitobot.listener.base.EventHandler
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.manager.CommandManager
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.PlainText

object WordCountListener : IListener {
    override val name: String = "群词云统计"

    @EventHandler
    fun countWord(event: GroupMessageEvent) {
        val group = Group.getGroupOrAdd(event.group.id)

        if (group.params["word_cloud"] != true) return
        event.message.forEach {
            when (it) {

                is PlainText -> if (CommandManager.getCommandPrefix(it.content) == "") {
                    group.wordList.add(it.content.trim())
                }
            }
        }

    }
}