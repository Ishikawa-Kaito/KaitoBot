package cn.zeshawn.kaitobot.entity

import cn.hutool.core.lang.UUID
import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.command.base.ChatCommand
import java.util.concurrent.ConcurrentHashMap

data class User(
    val userId: Long,
    val uuid: UUID,
    var role: UserRole,
    val currency: Map<String, Long> = ConcurrentHashMap()
) {
    companion object {
        private fun register(id: Long): User {
            KaitoMind.users[id].apply {
                val register = User(id, UUID.randomUUID(), UserRole.USER)
                return this ?: register.also { KaitoMind.users.putIfAbsent(id, register) }
            }
        }

        private fun getUser(id: Long): User? {
            return KaitoMind.users[id]
        }

        fun getUserOrRegister(qq: Long): User = getUser(qq) ?: register(qq)

    }
}

enum class UserRole {
    USER, ADMIN, DEV, OWNER;
}

fun User.hasPermission(cmd: ChatCommand): Boolean {
    return if (this.userId == KaitoMind.config.ownerId) {
        this.role = UserRole.OWNER
        true
    } else {
        this.role >= cmd.permission
    }
}