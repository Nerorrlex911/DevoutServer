package com.github.zimablue.devoutserver.lifecycle

import com.github.zimablue.devoutserver.util.execute
import java.lang.reflect.Method

class AwakeMethod(private val method: Method){
    private val awakeAnnotation: Awake = method.getAnnotation(Awake::class.java)
    val priority = awakeAnnotation.priority
    val lifeCycle = awakeAnnotation.lifeCycle
    fun execute() {
        method.execute()
    }
}