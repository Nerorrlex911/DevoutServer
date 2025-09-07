package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.feature.luckperms.LuckPerms.hasPermission
import com.github.zimablue.devoutserver.plugin.PluginManagerImpl
import com.github.zimablue.devoutserver.plugin.lifecycle.PluginLifeCycle
import com.github.zimablue.devoutserver.server.command.annotation.RegCommand
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

@RegCommand
object ReloadCommand : Command("reload") {
    private val pluginArg: ArgumentString = ArgumentType.String("plugin")
    init {
        setCondition { sender, s ->
            return@setCondition if(sender is Player) sender.hasPermission("devoutserver.command.reload") else true
        }
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
            plugin.lifeCycleManager.lifeCycle(PluginLifeCycle.RELOAD)
            sender.sendMessage("§aPlugin $pluginName reloaded")
        },pluginArg)
    }


}