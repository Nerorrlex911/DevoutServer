package com.github.zimablue.devoutserver.plugin.lifecycle

/**
 * `Awake` 注解用于标记某个方法在特定环节下执行的定义。
 *
 * 通过 `Awake` 注解，可以指定方法在特定的触发生命周期（`lifeCycle`）和执行优先级（`priority`）下被调用。
 * 这种机制可以用于定义和管理程序执行的生命周期或事件驱动的特定行为。
 *
 * 可应用于Kotlin单例函数、Java static方法、或Java中包含INSTANCE字段作为实例的类的方法。
 *
 * @property lifeCycle 触发方法执行的生命周期，通常用于指定触发时机。
 * @property priority 方法执行的优先级，允许控制多个方法在相同触发类型下的执行顺序。
 *
 * 示例用法：
 * ```
 * // 定义一个带有 `Awake` 注解的方法
 * @Awake(LifeCycle.LOAD, AwakePriority.NORMAL)
 * fun onStartup() {
 *     // 在初始化时执行
 * }
 * ```
 *
 * @see LifeCycle 定义了 `Awake` 注解中可用的生命周期。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Awake(val lifeCycle: PluginLifeCycle,val priority: AwakePriority)
