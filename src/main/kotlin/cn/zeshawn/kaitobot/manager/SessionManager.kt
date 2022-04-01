package cn.zeshawn.kaitobot.manager

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.command.base.ConversationCommand
import cn.zeshawn.kaitobot.entity.User
import cn.zeshawn.kaitobot.session.base.Session
import cn.zeshawn.kaitobot.session.base.SessionTarget
import cn.zeshawn.kaitobot.util.getLastingTimeAsString
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import java.time.Duration
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

object SessionManager {
    private val sessionPool: MutableSet<Session> = Collections.synchronizedSet(mutableSetOf())

    fun insertSession(session: Session): Boolean {
        KaitoMind.KaitoLogger.info("创建会话 ${session::class.java.simpleName + "#" + session.hashCode()}")
        return sessionPool.add(session)
    }

    fun removeSession(session: Session): Boolean {
        KaitoMind.KaitoLogger.info("移除会话 ${session::class.java.simpleName + "#" + session.hashCode()}")
        return sessionPool.remove(session)
    }

    fun hasSessionByGroup(groupID: Long): Boolean = getSessionsByGroup(groupID).isNotEmpty()

    fun hasSessionByGroup(groupID: Long, cmd: Class<*>): Boolean {
        return getSessionsByGroup(groupID).stream().filter { it.handler::class.java == cmd }.count() > 0
    }

    fun getSessionsByGroup(groupID: Long): List<Session> =
        sessionPool.stream().filter { it.target.groupId == groupID }.collect(Collectors.toList())

    fun hasSessionByID(id: Long): Boolean = getSessionsByID(id).isNotEmpty()

    fun hasSessionByID(id: Long, cmd: Class<*>): Boolean {
        return getSessionsByID(id).stream().filter { it.handler::class.java == cmd }.count() > 0
    }

    fun getSessionsByID(id: Long): List<Session> =
        sessionPool.stream().filter { it.target.privateId == id }.collect(Collectors.toList())

    fun getSessionsBySubject(id: Long): List<Session> =
        sessionPool.stream().filter { it.target.groupId == id || it.target.privateId == id }
            .collect(Collectors.toList())

    fun hasSessionBySubject(id: Long): Boolean = getSessionsBySubject(id).isNotEmpty()

    fun hasSessionBySubject(id: Long, cmd: Class<*>): Boolean {
        return getSessionsBySubject(id).stream().filter { it.handler::class.java == cmd }.count() > 0
    }

    fun getSessions(): MutableSet<Session> = sessionPool.toMutableSet()

    suspend fun handleSessions(e: MessageEvent, user: User): Boolean {
        val time = LocalDateTime.now()

        val target = if (e is GroupMessageEvent) {
            SessionTarget(e.group.id, e.sender.id)
        } else {
            SessionTarget(privateId = e.sender.id)
        }


        sessionPool.removeAll { Duration.between(it.lastActiveTime, time).seconds > 120 }  // 清除60s无人应答的会话

        val sessionStream = sessionPool.stream()
            .filter { it.target.groupId == target.groupId || it.target.privateId == target.privateId }

        val sessionToHandle = sessionStream.collect(Collectors.toList())

        if (sessionToHandle.isEmpty()) {
            return false
        }

        for (session in sessionToHandle) {
            if (CommandManager.getCommandPrefix(e.message.contentToString()).isEmpty()) {
                if (session.handler is ConversationCommand) {
                    session.lastActiveTime = LocalDateTime.now()
                    session.handler.handle(e, session, user)
                }
            }
        }

        if (sessionPool.stream()
                .filter { it.target.groupId == target.groupId || it.target.privateId == target.privateId }.count() > 0
        ) {
            KaitoMind.KaitoLogger.debug(
                "[会话] 处理 ${sessionToHandle.count()} 个会话耗时 ${
                    time.getLastingTimeAsString(
                        unit = TimeUnit.SECONDS,
                        msMode = true
                    )
                }"
            )
        }

        return sessionPool.stream()
            .filter { (it.target.groupId == target.groupId || it.target.privateId == target.privateId) }
            .count() > 0
    }

    fun removeDeprecatedSessions() {
        val time = LocalDateTime.now()
        val deprecatedSessions = sessionPool.filter { Duration.between(it.lastActiveTime, time).seconds > 120 }
        deprecatedSessions.forEach { }
        sessionPool.removeAll(deprecatedSessions.toSet())
    }

}