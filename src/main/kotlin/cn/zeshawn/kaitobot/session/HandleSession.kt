package cn.zeshawn.kaitobot.session

import cn.zeshawn.kaitobot.command.HandleCommand
import cn.zeshawn.kaitobot.service.HandleGame
import cn.zeshawn.kaitobot.session.base.Session
import cn.zeshawn.kaitobot.session.base.SessionTarget
import cn.zeshawn.kaitobot.session.base.SessionUser
import cn.zeshawn.kaitobot.session.base.getTarget
import kotlinx.coroutines.runBlocking

class HandleSession(override val target: SessionTarget, val hg: HandleGame) : Session(target, HandleCommand) {

    fun getAttender(id: Long): HandleSessionAttender? {
        users.forEach {
            if (it is HandleSessionAttender && it.id == id) {
                return it
            }
        }
        return null
    }


    override fun deprecated() {
        runBlocking {
            target.getTarget().sendMessage("太久没有人作答，正确答案是${hg.answer.word}。")
        }
    }
}

class HandleSessionAttender(override val id: Long, val username: String) : SessionUser(id) {
    @Volatile
    var answerTimes = 0
}