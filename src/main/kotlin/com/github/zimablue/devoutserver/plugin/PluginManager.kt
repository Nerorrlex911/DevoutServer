package com.github.zimablue.devoutserver.plugin

import com.github.zimablue.devoutserver.util.map.KeyMap
import net.minestom.server.ServerProcess

abstract class PluginManager: LinkedHashMap<String, Plugin>() {
    abstract fun shutdown()
    abstract fun start()
    abstract fun load()
    abstract fun init(serverProcess: ServerProcess)
    abstract fun enable()
    abstract fun active()
}