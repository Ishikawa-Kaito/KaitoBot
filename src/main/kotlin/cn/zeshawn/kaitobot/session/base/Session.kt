package cn.zeshawn.kaitobot.session.base

import cn.zeshawn.kaitobot.command.base.ChatCommand
import java.time.LocalDateTime

open class Session(
    open val target: SessionTarget,
    val handler: ChatCommand,
) {
    val users: MutableSet<SessionUser> = mutableSetOf()
    val createdTime: LocalDateTime = LocalDateTime.now()
    var lastActiveTime: LocalDateTime = LocalDateTime.now()
}

data class SessionTarget(
    val groupId: Long = 0,
    val privateId: Long = 0,


    )

open class SessionUser(
    open val id: Long
)