package com.github.zimablue.devoutserver.plugin.annotation

import com.github.zimablue.devoutserver.annotation.AnnotationManagerImpl
import com.github.zimablue.devoutserver.plugin.Plugin
import com.github.zimablue.devoutserver.util.ClassUtil
import java.lang.reflect.Field
import java.lang.reflect.Method

object AnnotationManager {
    val pluginClassMap = hashMapOf<Plugin,Set<Class<*>>>()

    inline fun <reified V: Annotation> getTargets(plugin: Plugin): Triple<HashSet<Field>, HashSet<Method>, HashSet<Class<*>>> {
        // 在设置packageName的情况下再扫描注解
        if(plugin.origin.packageName == null) {
            return Triple(hashSetOf(), hashSetOf(), hashSetOf())
        }
        val classesToScan = pluginClassMap.getOrPut(plugin) {
            ClassUtil.getClasses(plugin.javaClass,plugin.origin.packageName).toSet()
        }
        return AnnotationManagerImpl.getTargets<V>(classesToScan)
    }
}