package com.github.zimablue.devoutserver.api.lifecycle

interface LifeCycleTask {
    fun priority(): Int
    fun run()
}