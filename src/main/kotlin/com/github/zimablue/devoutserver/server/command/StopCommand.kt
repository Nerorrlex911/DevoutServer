package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.DevoutServer
import com.github.zimablue.devoutserver.server.command.annotation.RegCommand
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.Command

@RegCommand
object StopCommand : Command("stop") {
    init {
        setDefaultExecutor{ sender,context ->
            sender.sendMessage(Component.text("Server Stopping"))
            DevoutServer.shutdown()
        }
    }

}