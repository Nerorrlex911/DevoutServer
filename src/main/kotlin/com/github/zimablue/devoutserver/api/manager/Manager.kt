package com.github.zimablue.devoutserver.api.manager

import com.github.zimablue.devoutserver.api.decouple.SubPouvoir
import com.github.zimablue.devoutserver.api.decouple.map.component.Registrable

interface Manager : Registrable<String>, Comparable<Manager> {
    val priority: Int
    val subPouvoir: SubPouvoir

    fun onLoad() {
    }

    fun onEnable() {}
    fun onActive() {}

    fun onReload() {}
    fun onDisable() {}

    override fun register() {}

    override fun compareTo(other: Manager): Int {
        return if (priority == other.priority) 0
        else if (priority > other.priority) 1
        else -1
    }

}