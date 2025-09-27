package com.github.zimablue.devoutserver.plugin.lang

import com.github.zimablue.devoutserver.lang.LangManager
import com.github.zimablue.devoutserver.plugin.Plugin
import com.github.zimablue.devoutserver.plugin.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.plugin.lifecycle.PluginLifeCycle
import java.io.File

open class PluginLangManager(
    val plugin: Plugin,
    val file: File=plugin.dataDirectory.resolve("lang").toFile(),
) : LangManager() {
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
        onReload()
    }
    open fun onDisable() {

    }
    open fun onReload() {
        reload(file)
    }
}