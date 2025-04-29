package com.github.zimablue.devoutserver.plugin.lifecycle


import com.github.zimablue.devoutserver.plugin.Plugin
import com.github.zimablue.devoutserver.plugin.annotation.AnnotationManager
import com.github.zimablue.devoutserver.util.execute
import java.util.*


class PluginLifeCycleManager(val plugin: Plugin) {
    var currentLifeCycle: PluginLifeCycle = PluginLifeCycle.NONE
    private val lifeCycleTasks = mutableMapOf<PluginLifeCycle,LinkedList<LifeCycleTask>>()
    fun registerTask(task: LifeCycleTask) {
        lifeCycleTasks.computeIfAbsent(task.lifeCycle) {
            LinkedList<LifeCycleTask>()
        }.apply {
            add(task)
            sortBy{it.priority}
        }

        //如果注册任务应当执行的生命周期已经过去，就现场运行
        if(task.lifeCycle<currentLifeCycle) {
            task.callback.run()
        }
    }
    fun registerTask(lifecycle: PluginLifeCycle,priority: AwakePriority, task: Runnable) {
        registerTask(LifeCycleTask(lifecycle,priority,task))
    }
    fun lifeCycle(lifeCycle: PluginLifeCycle) {
        currentLifeCycle = lifeCycle
        lifeCycleTasks[lifeCycle]?.forEach { it.execute() }
    }
    init {
        AnnotationManager.addPlugin(plugin)
        val allMethods = AnnotationManager.getTargets<Awake>(plugin).second
        allMethods.forEach { method ->
            val annotation = method.getAnnotation(Awake::class.java)
            registerTask(LifeCycleTask(annotation.lifeCycle,annotation.priority) {
                method.execute()
            })
        }
    }
}