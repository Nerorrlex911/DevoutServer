package com.github.zimablue.devoutserver.api.plugin.manager

import com.github.zimablue.devoutserver.api.lifecycle.LifeCycle
import com.github.zimablue.devoutserver.api.lifecycle.LifeCycleTask

/**
 * 生命周期管理器
 * 每个Plugin对象持有一个LifeCycleManager对象
 * 用于
 */
class LifeCycleManager {

    var isStopped = false

    var currentLifeCycle = LifeCycle.NONE

    val lifeCycleTasks = mutableMapOf<LifeCycle,MutableList<LifeCycleTask>>()

    /**
     * 推迟任务到指定生命周期下执行，如果生命周期已经过去则立即执行
     *
     * @param lifeCycle 生命周期
     * @param lifeCycleTask  任务
     */
    fun registerLifeCycleTask(lifeCycle: LifeCycle, lifeCycleTask: LifeCycleTask) {
        if(isStopped) return
        if(currentLifeCycle.ordinal>=lifeCycle.ordinal) {
            lifeCycleTask.run()
        } else {
            val tasks = lifeCycleTasks.computeIfAbsent(lifeCycle) { mutableListOf() }
            tasks.add(lifeCycleTask)
            tasks.sortBy { it.priority() }
        }
    }

    /**
     * 推迟任务到指定生命周期下执行，如果生命周期已经过去则立即执行
     *
     * @param lifeCycle 生命周期
     * @param priority 优先级
     * @param run 运行任务
     */
    fun registerLifeCycleTask(lifeCycle: LifeCycle,priority: Int,run: Runnable) {
        if(isStopped) return
        registerLifeCycleTask(lifeCycle,object : LifeCycleTask {
            override fun priority(): Int {
                return priority
            }
            override fun run() {
                run.run()
            }
        })
    }

    /**
     * 执行生命周期任务
     */
    fun lifeCycle(lifeCycle: LifeCycle) {
        if (isStopped) return
        currentLifeCycle = lifeCycle
        lifeCycleTasks.remove(lifeCycle)?.forEach { it.run() }
    }
}