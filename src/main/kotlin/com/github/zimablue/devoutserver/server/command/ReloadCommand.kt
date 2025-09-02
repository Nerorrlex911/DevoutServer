package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.plugin.PluginManagerImpl
import com.github.zimablue.devoutserver.server.command.annotation.RegCommand
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType

@RegCommand
object ReloadCommand : Command("reload") {
    val pluginArg = ArgumentType.String("plugin")
    init {
        setDefaultExecutor { sender, context ->
            sender.sendMessage("§a/reload <plugin> call Reload LifeCycle of a plugin")
        }
        addSyntax({ sender, context ->
            val pluginName = context.get(pluginArg)
            val plugin = PluginManagerImpl[pluginName]
            if (plugin == null) {
                sender.sendMessage("§cPlugin $pluginName not found")
                return@addSyntax
            }
            plugin.onReload()
            sender.sendMessage("§aPlugin $pluginName reloaded")
        },pluginArg)
    }
}