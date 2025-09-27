package com.github.zimablue.devoutserver.plugin.script


import com.github.zimablue.devoutserver.DevoutServer
import com.github.zimablue.devoutserver.plugin.Plugin
import com.github.zimablue.devoutserver.plugin.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.plugin.lifecycle.PluginLifeCycle
import com.github.zimablue.devoutserver.script.ScriptManager
import java.io.File
import javax.script.ScriptEngine

open class PluginScriptManager(
    val plugin: Plugin,
    path: File=plugin.dataDirectory.resolve("scripts").toFile(),
    loadLib: ScriptEngine.() -> Unit={}
): ScriptManager(path,plugin.logger,plugin.pluginClassLoader,loadLib) {
    // init应当手动调用，以防止提前调用生命周期管理器导致问题
    open fun init() {
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

    open fun onEnable() {
        loadScripts()
        compiledScripts.forEach { (name, _) ->
            plugin.logger.info("Enabling script $name")
            run(name,"onEnable")
        }
    }
    open fun onDisable() {
        compiledScripts.forEach { (name, _) ->
            plugin.logger.info("Disabling script $name")
            run(name,"onDisable")
        }
    }
    open fun onReload() {
        reload()
        compiledScripts.forEach { (name, _) ->
            plugin.logger.info("Reloading script $name")
            run(name,"onReload")
        }
    }

}