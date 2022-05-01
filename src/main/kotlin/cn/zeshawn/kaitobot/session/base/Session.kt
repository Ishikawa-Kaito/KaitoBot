package cn.zeshawn.kaitobot.session.base

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.command.base.ChatCommand
import net.mamoe.mirai.contact.Contact
import java.time.LocalDateTime

open class Session(
    open val target: SessionTarget,
    val handler: ChatCommand,
    open val timeout: Int = 120,
) {
    val users: MutableSet<SessionUser> = mutableSetOf()
    val createdTime: LocalDateTime = LocalDateTime.now()
    var lastActiveTime: LocalDateTime = LocalDateTime.now()

    open fun deprecated() {}
}

data class SessionTarget(
    val groupId: Long = 0,
    val privateId: Long = 0,
)

open class SessionUser(
    open val id: Long
)


fun SessionTarget.getTarget(): Contact {
    if (this.groupId != 0L) {
        return KaitoMind.kaito.getBot().getGroupOrFail(this.groupId)
    } else if (this.privateId != 0L) {
        return KaitoMind.kaito.getBot().getFriendOrFail(this.privateId)
    }
    return KaitoMind.kaito.getBot().asFriend
}