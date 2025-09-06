package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.DevoutServer
import com.github.zimablue.devoutserver.feature.luckperms.LuckPerms.hasPermission
import com.github.zimablue.devoutserver.server.command.annotation.RegCommand
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.Command
import net.minestom.server.entity.Player

@RegCommand
object StopCommand : Command("stop") {
    init {
        setCondition { sender, s ->
            return@setCondition if(sender is Player) sender.hasPermission("devoutserver.stop") else true
        }
        setDefaultExecutor{ sender,context ->
            sender.sendMessage(Component.text("Server Stopping"))
            DevoutServer.shutdown()
        }
    }

}