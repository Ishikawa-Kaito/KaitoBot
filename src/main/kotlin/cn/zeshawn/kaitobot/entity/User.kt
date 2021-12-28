package cn.zeshawn.kaitobot.entity

import cn.hutool.core.lang.UUID
import cn.zeshawn.kaitobot.KaitoMind
import java.util.*

data class User(
    val userId:Long,
    val uuid:UUID,
    val role:UserRole
){
    companion object{
        fun register(id:Long):User{
            KaitoMind.users[id].apply {
                val register = User(id, UUID.randomUUID(),UserRole.USER)
                return this ?: register.also { KaitoMind.users.putIfAbsent(id, register) }
            }
        }

        fun getUser(id: Long): User? {
            return KaitoMind.users[id]
        }

        fun getUserOrRegister(qq: Long): User = getUser(qq) ?: register(qq)
    }
}

enum class UserRole {
    USER, ADMIN, DEV, OWNER;
}