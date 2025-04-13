package com.github.zimablue.devoutserver.plugin.lifecycle



class LifeCycleTask(val lifeCycle: PluginLifeCycle,val priority: AwakePriority, val callback: Runnable) {
    fun execute() {
        callback.run()
    }
}