package com.github.zimablue.devoutserver.api.plugin.manager

import com.github.zimablue.devoutserver.api.plugin.Plugin
import net.minestom.server.ServerProcess

abstract class PluginManager: MutableMap<String,Plugin> by mutableMapOf() {
    abstract fun shutdown()
    abstract fun start()
    abstract fun gotoPreInit()
    abstract fun init(serverProcess: ServerProcess)
    abstract fun gotoInit()
    abstract fun gotoPostInit()
}