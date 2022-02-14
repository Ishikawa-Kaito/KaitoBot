package cn.zeshawn.kaitobot.entity

import cn.zeshawn.kaitobot.KaitoMind

data class Group(
    val id: Long,
    val params: MutableMap<String, Any> = mutableMapOf(),
    val wordList: MutableList<String> = mutableListOf()
) {
    companion object {
        private fun addGroup(id: Long): Group {
            KaitoMind.groups[id].apply {
                val newGroup = Group(id)
                return this ?: newGroup.also { KaitoMind.groups.putIfAbsent(id, newGroup) }
            }
        }

        private fun getGroup(id: Long): Group? {
            return KaitoMind.groups[id]
        }

        fun modifyGroup(newGroup: Group): Boolean {
            if (getGroup(newGroup.id) != null) {
                KaitoMind.groups[newGroup.id] = newGroup
                return true
            }
            return false
        }

        fun getGroupOrAdd(id: Long): Group = getGroup(id) ?: addGroup(id)
    }
}