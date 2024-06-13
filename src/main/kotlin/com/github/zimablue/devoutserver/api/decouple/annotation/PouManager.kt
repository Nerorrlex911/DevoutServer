package com.github.zimablue.devoutserver.api.decouple.annotation

/**
 * Pou manager
 *
 * @constructor
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PouManager(val path: String = "")
