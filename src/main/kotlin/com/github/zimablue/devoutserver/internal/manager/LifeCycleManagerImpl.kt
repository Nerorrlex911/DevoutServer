package com.github.zimablue.devoutserver.internal.manager

import com.github.zimablue.devoutserver.api.annotation.AnnotationRange
import com.github.zimablue.devoutserver.internal.core.lifecycle.Awake
import com.github.zimablue.devoutserver.internal.core.lifecycle.AwakeMethod
import com.github.zimablue.devoutserver.internal.core.lifecycle.LifeCycle
import java.lang.reflect.Method
import java.util.*


object LifeCycleManagerImpl {
    var isStopped = false
    val currentLifeCycle = LifeCycle.NONE
    private val awakeMethods = mutableMapOf<LifeCycle, LinkedList<AwakeMethod>>()
    init {
        val allMethods = AnnotationManagerImpl.getTargets<Awake>(AnnotationRange.CORE).second
        allMethods.forEach { registerMethod(it) }
        awakeMethods.forEach { (_, u) -> u.sortBy { it.priority.ordinal } }
    }
    fun registerMethod(method: Method) {
        val awakeMethod = AwakeMethod(method)
        awakeMethods.computeIfAbsent(awakeMethod.lifeCycle) { LinkedList() }.add(awakeMethod)
    }
    fun lifeCycle(lifeCycle: LifeCycle) {
        if(!isStopped) awakeMethods[lifeCycle]?.forEach { it.execute() }

    }
}