package com.github.zimablue.devoutserver.annotation

import com.github.zimablue.devoutserver.util.ClassUtil
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.annotation.AnnotationTarget.*

object AnnotationManagerImpl{
    val coreClasses = ClassUtil.getClasses("com.github.zimablue.devoutserver")

    inline fun <reified V: Annotation> getTargets(classesToScan: Set<Class<*>> = coreClasses): Triple<HashSet<Field>, HashSet<Method>, HashSet<Class<*>>> {
        val annotation = V::class.java
        val targetAnnotation = annotation.getAnnotation(Target::class.java)
        val allowed = targetAnnotation.allowedTargets.toSet()
        val fields: HashSet<Field> = hashSetOf()
        val methods: HashSet<Method> = hashSetOf()
        val classes: HashSet<Class<*>> = hashSetOf()
        allowed.forEach { type ->
            when(type) {
                CLASS -> {
                    classes.addAll(classesToScan.filter { it.isAnnotationPresent(annotation) })
                }
                FIELD -> {
                    classesToScan.forEach {
                        fields.addAll(ClassUtil.getAnnotationMember(it, annotation))
                    }
                }
                FUNCTION -> {
                    classesToScan.forEach {
                        methods.addAll(ClassUtil.getAnnotationMethod(it, annotation))
                    }
                }
                else -> {}
            }
        }
        return Triple(fields, methods, classes)
    }
}