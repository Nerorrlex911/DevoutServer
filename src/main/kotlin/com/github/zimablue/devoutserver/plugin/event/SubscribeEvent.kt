package com.github.zimablue.devoutserver.plugin.event

/**
 * 订阅事件注解 [@SubscribeEvent]，用于标记事件订阅方法。
 * @property priority 这其实是事件执行的优先级，类似于Bukkit的EventPriority
 * @property ignoreCancelled 是否忽略已取消的事件，默认为 false。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class SubscribeEvent(
    val priority: EventPriority = EventPriority.NORMAL,
    val ignoreCancelled: Boolean = false
)
