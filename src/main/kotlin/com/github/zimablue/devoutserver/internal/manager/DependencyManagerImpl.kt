package com.github.zimablue.devoutserver.internal.manager

/**
 * 管理核心本身的依赖
 */
object DependencyManagerImpl: Manager {
    override val priority: Int = 0
    override val key: String = "DependencyManager"
    init {

    }
}