package cn.zeshawn.kaitobot.service

import cn.zeshawn.kaitobot.entity.User
import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.NormalMember

object BankService {


    fun getBalance(id: Long, currency: String): Long {
        val user = User.getUserOrRegister(id)
        if (currency !in user.currency.keys) {
            user.currency[currency] = 0
        }
        return user.currency.getOrDefault(currency, 0)
    }

    fun purchase(id: Long, currency: String, cost: Long): Boolean {
        val user = User.getUserOrRegister(id)
        val balance = getBalance(id, currency)
        if (cost > balance) return false
        user.currency[currency] = user.currency[currency]!!.minus(cost)
        return true
    }

    fun addMoney(id: Long, currency: String, money: Long) {
        val user = User.getUserOrRegister(id)
        if (currency !in user.currency.keys) {
            user.currency[currency] = money
        } else {
            user.currency[currency] = user.currency[currency]!!.plus(money)
        }
    }

    fun getRank(members: ContactList<NormalMember>, currency: String): List<Pair<Long, Long>> {
        return buildList {
            members.forEach {
                val user = User.getUserOrRegister(it.id)
                if (currency !in user.currency.keys) {
                    user.currency[currency] = 0
                }
                val balance = user.currency.getOrDefault(currency, 0)
                add((it.id to balance))
            }
        }.sortedByDescending { it.second }.subList(0, minOf(5, members.size))
    }

    fun getDCoin(id: Long) = getBalance(id, "斗币")

    fun addDCoin(id: Long, money: Long) = addMoney(id, "斗币", money)

    fun purchaseDCoin(id: Long, cost: Long) = purchase(id, "斗币", cost)

    fun getDCoinRank(members: ContactList<NormalMember>) = getRank(members, "斗币")

}