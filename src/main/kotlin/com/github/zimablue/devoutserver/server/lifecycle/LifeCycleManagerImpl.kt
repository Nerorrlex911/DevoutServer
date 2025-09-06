package com.github.zimablue.devoutserver.server.lifecycle

import com.github.zimablue.devoutserver.annotation.AnnotationManagerImpl
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList


object LifeCycleManagerImpl {
    private var isStopped = false
    private var currentLifeCycle = LifeCycle.NONE
    private val awakeMethods = mutableMapOf<LifeCycle, CopyOnWriteArrayList<AwakeMethod>>()
    init {
        val allMethods = AnnotationManagerImpl.getTargets<Awake>().second
        allMethods.forEach { registerMethod(it) }
        awakeMethods.forEach { (_, u) -> u.sortBy { it.priority } }
    }
    fun registerMethod(method: Method) {
        val awakeMethod = AwakeMethod(method)
        awakeMethods.computeIfAbsent(awakeMethod.lifeCycle) { CopyOnWriteArrayList() }.add(awakeMethod)
        //如果已进入该周期，立即执行
        if (awakeMethod.lifeCycle <= currentLifeCycle) {
            awakeMethod.execute()
        }
    }
    fun lifeCycle(lifeCycle: LifeCycle) {
        if(!isStopped) awakeMethods[lifeCycle]?.forEach { it.execute() }
        currentLifeCycle = lifeCycle

    }
}