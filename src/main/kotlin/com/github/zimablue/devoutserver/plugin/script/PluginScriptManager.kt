package com.github.zimablue.devoutserver.plugin.script


import com.github.zimablue.devoutserver.plugin.Plugin
import com.github.zimablue.devoutserver.plugin.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.plugin.lifecycle.PluginLifeCycle
import com.github.zimablue.devoutserver.script.ScriptManager
import java.io.File

class PluginScriptManager(val plugin: Plugin,path: File=plugin.dataDirectory.resolve("scripts").toFile()): ScriptManager(path,plugin.logger) {
    init {
        with(plugin.lifeCycleManager) {
            registerTask(
                PluginLifeCycle.ENABLE,
                AwakePriority.NORMAL
            ) { onEnable() }
            registerTask(
                PluginLifeCycle.DISABLE,
                AwakePriority.NORMAL
            ) { onDisable() }
            registerTask(
                PluginLifeCycle.RELOAD,
                AwakePriority.NORMAL
            ) { onReload() }
        }
    }

    fun onEnable() {
        loadScripts()
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
    fun onReload() {
        reload()
        compiledScripts.forEach { (name, _) ->
            plugin.logger.info("Reloading script $name")
            run(name,"onReload")
        }
    }

}