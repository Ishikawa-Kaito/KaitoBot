package cn.zeshawn.kaitobot.session

import cn.zeshawn.kaitobot.command.WordleCommand
import cn.zeshawn.kaitobot.service.WordleGame
import cn.zeshawn.kaitobot.session.base.Session
import cn.zeshawn.kaitobot.session.base.SessionTarget
import cn.zeshawn.kaitobot.session.base.SessionUser
import cn.zeshawn.kaitobot.session.base.getTarget
import kotlinx.coroutines.runBlocking

class WordleSession(override val target: SessionTarget, val wg: WordleGame, override val timeout: Int) :
    Session(target, WordleCommand) {
    fun getAttender(id: Long): WordleSessionAttender? {
        users.forEach {
            if (it is WordleSessionAttender && it.id == id) {
                return it
            }
        }
        return null
    }


    override fun deprecated() {
        runBlocking {
            target.getTarget().sendMessage("What took you so long? The answer is ${wg.answer.lowercase()}。")
        }
    }
}

class WordleSessionAttender(override val id: Long, val username: String) : SessionUser(id) {
    @Volatile
    var answerTimes = 0
}