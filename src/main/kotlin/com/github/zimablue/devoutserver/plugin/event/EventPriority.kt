package com.github.zimablue.devoutserver.plugin.event

import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode

enum class EventPriority(val priority: Int) {

    LOWEST(-24),
    LOW(-12),
    NORMAL(0),
    HIGH(12),
    HIGHEST(24),
    MONITOR(48);

    val node: EventNode<Event> = EventNode.all(this.name).setPriority(priority)

    companion object {
        internal fun registerPriorities() {
            for (priority in entries) {
                MinecraftServer.getGlobalEventHandler().addChild(priority.node)
            }
        }
    }
}