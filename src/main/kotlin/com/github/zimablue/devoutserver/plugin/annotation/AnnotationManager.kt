package com.github.zimablue.devoutserver.plugin.annotation

import com.github.zimablue.devoutserver.annotation.AnnotationManagerImpl
import com.github.zimablue.devoutserver.plugin.Plugin
import com.github.zimablue.devoutserver.util.ClassUtil
import java.lang.reflect.Field
import java.lang.reflect.Method

object AnnotationManager {
    val pluginClassMap = hashMapOf<Plugin,Set<Class<*>>>()
    fun addPlugin(plugin: Plugin) {
        pluginClassMap[plugin] = ClassUtil.getClasses(plugin.javaClass).toSet()
    }
    inline fun <reified V: Annotation> getTargets(plugin: Plugin): Triple<HashSet<Field>, HashSet<Method>, HashSet<Class<*>>> {
        val classesToScan = pluginClassMap[plugin]?:return Triple(hashSetOf(), hashSetOf(), hashSetOf())
        return AnnotationManagerImpl.getTargets<V>(classesToScan)
    }
}