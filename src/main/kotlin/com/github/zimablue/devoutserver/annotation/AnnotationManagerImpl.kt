package com.github.zimablue.devoutserver.annotation

import com.github.zimablue.devoutserver.plugin.Plugin
import com.github.zimablue.devoutserver.util.ClassUtil
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.annotation.AnnotationTarget.*

object AnnotationManagerImpl: AnnotationManager() {
    override val priority: Int = 0
    override val key: String = "AnnotationManager"
    val pluginClassMap = mutableMapOf<Plugin,Set<Class<*>>>()//TODO: put plugin class in the map
    val pluginClasses
        get() = pluginClassMap.values.flatten().toSet()
    val coreClasses = ClassUtil.getClasses("com.github.zimablue.devoutserver")

    inline fun <reified V: Annotation> getTargets(range: AnnotationRange): Triple<HashSet<Field>, HashSet<Method>, HashSet<Class<*>>> {
        val annotation = V::class.java
        val targetAnnotation = annotation.getAnnotation(Target::class.java)
        val allowed = targetAnnotation.allowedTargets.toSet()
        val fields: HashSet<Field> = hashSetOf()
        val methods: HashSet<Method> = hashSetOf()
        val classes: HashSet<Class<*>> = hashSetOf()
        val scannedClasses = when(range) {
            AnnotationRange.PLUGIN -> pluginClasses
            AnnotationRange.CORE -> coreClasses
            AnnotationRange.ALL -> pluginClasses + coreClasses

        }
        allowed.forEach { type ->
            when(type) {
                CLASS -> {
                    classes.addAll(scannedClasses.filter { it.isAnnotationPresent(annotation) })
                }
                FIELD -> {
                    scannedClasses.forEach {
                        fields.addAll(ClassUtil.getAnnotationMember(it, annotation))
                    }
                }
                FUNCTION -> {
                    scannedClasses.forEach {
                        methods.addAll(ClassUtil.getAnnotationMethod(it, annotation))
                    }
                }
                else -> {}
            }
        }
        return Triple(fields, methods, classes)
    }
}