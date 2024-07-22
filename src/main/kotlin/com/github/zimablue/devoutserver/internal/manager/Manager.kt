package com.github.zimablue.devoutserver.internal.manager

interface Manager : Comparable<Manager>{
    val priority: Int
    val key: String
    fun register() {}
    override fun compareTo(other: Manager): Int {
        return if (priority == other.priority) 0
        else if (priority > other.priority) 1
        else -1
    }
}