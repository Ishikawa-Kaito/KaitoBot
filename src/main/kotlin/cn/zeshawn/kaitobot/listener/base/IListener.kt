package cn.zeshawn.kaitobot.listener.base

import cn.zeshawn.kaitobot.KaitoMind
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.globalEventChannel
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf


interface IListener {
    val name: String
}


fun IListener.register(bot: Bot) {
    val clazz = this::class

    val methodEvent = mutableMapOf<KClass<out Event>, KFunction<*>>()

    clazz.functions.forEach {
        if (it.annotations.find { clazz -> clazz.annotationClass == EventHandler::class } == null) {
            return@forEach
        } else {
            it.parameters.forEach { kp ->
                val eventClass = kp.type.classifier

                if (kp.type.isSubtypeOf(net.mamoe.mirai.event.Event::class.createType()) && eventClass != null && eventClass is KClass<*>) {
                    @Suppress("UNCHECKED_CAST")
                    methodEvent[eventClass as KClass<out Event>] = it
                }
            }
        }
    }

    if (name.isEmpty() || methodEvent.isEmpty()) {
        KaitoMind.KaitoLogger.warn("监听器 ${clazz.java.simpleName} 没有监听任何一个事件!")
        return
    } else {
        methodEvent.forEach { (clazz, method) ->
            if (clazz.isSubclassOf(Event::class)) {
                bot.globalEventChannel().subscribeAlways(clazz) { subEvent ->
                    if (subEvent.filterDebug())
                        method.call(this@register, subEvent)
                }
            }
        }
    }

    KaitoMind.KaitoLogger.info("[Listener] 已注册 $name 监听器")
}

fun Event.filterDebug(): Boolean {
    if (KaitoMind.debug) {
        this.javaClass.methods.forEach {
            if (it.name in listOf("getSubject")) {
                if ((it.invoke(this) as Group).id == 1035268136L) {
                    return true
                }
            }
        }
        return false
    }
    return true
}
