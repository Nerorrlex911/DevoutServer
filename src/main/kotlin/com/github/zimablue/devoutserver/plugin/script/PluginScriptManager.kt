package com.github.zimablue.devoutserver.plugin.script

import com.github.zimablue.devoutserver.DevoutServer.nashornHooker
import com.github.zimablue.devoutserver.plugin.Plugin
import com.github.zimablue.devoutserver.plugin.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.plugin.lifecycle.PluginLifeCycle
import com.github.zimablue.devoutserver.script.ScriptManager
import java.io.File

class PluginScriptManager(val plugin: Plugin,path: File): ScriptManager(path) {
    init {
        with(plugin.lifeCycleManager) {
            registerTask(
                PluginLifeCycle.NONE,
                AwakePriority.NORMAL
            ) { onInit() }
            registerTask(
                PluginLifeCycle.LOAD,
                AwakePriority.NORMAL
            ) { onLoad() }
            registerTask(
                PluginLifeCycle.ENABLE,
                AwakePriority.NORMAL
            ) { onEnable() }
            registerTask(
                PluginLifeCycle.DISABLE,
                AwakePriority.NORMAL
            ) { onDisable() }
        }
    }

    fun onInit() {
        plugin.savePackagedResource(path.toPath())
    }

    fun onLoad() {
        loadScripts()
    }

    fun onEnable() {
        compiledScripts.forEach { (name, _) ->
            plugin.logger.info("Enabling script $name")
            run(name,"onEnable")
        }
    }
    fun onDisable() {
        compiledScripts.forEach { (name, _) ->
            plugin.logger.info("Disabling script $name")
            run(name,"onDisable")
        }
    }

}