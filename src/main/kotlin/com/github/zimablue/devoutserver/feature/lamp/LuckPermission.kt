package com.github.zimablue.devoutserver.feature.lamp

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class LuckPermission(val value: String)
