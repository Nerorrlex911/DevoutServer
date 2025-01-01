package com.github.zimablue.devoutserver.plugin

import com.github.zimablue.devoutserver.util.map.KeyMap
import net.minestom.server.ServerProcess

abstract class PluginManager: KeyMap<String, Plugin>() {
    abstract fun shutdown()
    abstract fun start()
    abstract fun gotoPreInit()
    abstract fun init(serverProcess: ServerProcess)
    abstract fun gotoInit()
    abstract fun gotoPostInit()
}