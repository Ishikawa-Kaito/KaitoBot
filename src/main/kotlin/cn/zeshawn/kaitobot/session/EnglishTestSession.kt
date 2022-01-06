package cn.zeshawn.kaitobot.session

import cn.zeshawn.kaitobot.command.EnglishTestCommand
import cn.zeshawn.kaitobot.data.Word
import cn.zeshawn.kaitobot.session.base.Session
import cn.zeshawn.kaitobot.session.base.SessionTarget
import cn.zeshawn.kaitobot.session.base.SessionUser
import java.time.Duration
import java.time.LocalDateTime

class EnglishTestSession(override val target: SessionTarget, val answer: Word) : Session(target, EnglishTestCommand) {

    lateinit var usedTime: Duration
    lateinit var lastAnswerTime: LocalDateTime

    fun getAttender(id: Long): EnglishTestAttender? {
        users.forEach {
            if (it is EnglishTestAttender && it.id == id) {
                return it
            }
        }
        return null
    }
}

class EnglishTestAttender(override val id: Long, val username: String) : SessionUser(id) {
    @Volatile
    var answerTimes = 0
}