package com.github.zimablue.devoutserver.plugin.script

import com.github.zimablue.devoutserver.DevoutServer.nashornHooker
import com.github.zimablue.devoutserver.plugin.Plugin
import com.github.zimablue.devoutserver.plugin.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.plugin.lifecycle.PluginLifeCycle
import com.github.zimablue.devoutserver.script.ScriptManager
import java.io.File

class PluginScriptManager(val plugin: Plugin,path: File): ScriptManager(path) {
    init {
        plugin.lifeCycleManager.registerTask(
            PluginLifeCycle.ENABLE,
            AwakePriority.NORMAL
        ) { onEnable() }
        plugin.lifeCycleManager.registerTask(
            PluginLifeCycle.DISABLE,
            AwakePriority.NORMAL,
        ) { onDisable() }
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

    fun run(name: String,function: String) {
        run(name,function, null)
    }

    fun run(name: String,function: String,map: Map<String,Any>?,vararg args: Any) {
        val script = compiledScripts[name] ?: return
        if (nashornHooker.isFunction(script.scriptEngine, function)) {
            try {
                script.invoke(function, map, *args)
            } catch (error: Throwable) {
                plugin.logger.error("Error in $function of ${script.name}")
                error.printStackTrace()
            }
        }
    }

}