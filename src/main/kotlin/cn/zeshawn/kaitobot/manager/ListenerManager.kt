package cn.zeshawn.kaitobot.manager

import cn.zeshawn.kaitobot.KaitoMind
import cn.zeshawn.kaitobot.listener.base.IListener
import cn.zeshawn.kaitobot.listener.base.register
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder

object ListenerManager {


    public fun setup() {
        val reflection = Reflections(ConfigurationBuilder().forPackage("cn.zeshawn.kaitobot.listener"))
        reflection.getSubTypesOf(IListener::class.java)
            .map { it.kotlin }
            .mapNotNull { it.objectInstance }
            .toTypedArray()
            .forEach {
                it.register(KaitoMind.kaito.getBot())
            }
    }
}