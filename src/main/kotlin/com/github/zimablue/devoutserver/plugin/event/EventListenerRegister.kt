package com.github.zimablue.devoutserver.plugin.event

import com.github.zimablue.devoutserver.plugin.PluginManagerImpl
import com.github.zimablue.devoutserver.plugin.annotation.AnnotationManager
import com.github.zimablue.devoutserver.server.lifecycle.Awake
import com.github.zimablue.devoutserver.server.lifecycle.LifeCycle
import net.minestom.server.event.Event
import net.minestom.server.event.EventListener

object EventListenerRegister {
    @Awake(LifeCycle.LOAD)
    fun onLoad() {
        EventPriority.registerPriorities()
        PluginManagerImpl.values.forEach { plugin ->
            val methods = AnnotationManager.getTargets<SubscribeEvent>(plugin).second
            methods.forEach { method ->
                val annotation = method.getAnnotation(SubscribeEvent::class.java)
                val ignoreCancelled = annotation.ignoreCancelled

                // 安全地将参数类型转换为 Event 类
                @Suppress("UNCHECKED_CAST")
                val eventClass = (method.parameterTypes[0] as? Class<Event>)
                    ?: throw IllegalArgumentException("${method.name} 参数不正确, Class: ${method.declaringClass.name}, Plugin: ${plugin.name}")
                // 构建事件监听器
                val listener = EventListener.builder(eventClass).
                ignoreCancelled(ignoreCancelled).
                handler { clazz ->
                    method.invoke(null,clazz)
                }.build()
                annotation.priority.node.addListener(listener)
            }
        }
    }
}