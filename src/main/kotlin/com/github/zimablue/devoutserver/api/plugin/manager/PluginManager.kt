package com.github.zimablue.devoutserver.api.plugin.manager

import com.github.zimablue.devoutserver.api.map.KeyMap
import com.github.zimablue.devoutserver.api.plugin.Plugin
import net.minestom.server.ServerProcess

abstract class PluginManager: KeyMap<String, Plugin>() {
    abstract fun shutdown()
    abstract fun start()
    abstract fun gotoPreInit()
    abstract fun init(serverProcess: ServerProcess)
    abstract fun gotoInit()
    abstract fun gotoPostInit()
}