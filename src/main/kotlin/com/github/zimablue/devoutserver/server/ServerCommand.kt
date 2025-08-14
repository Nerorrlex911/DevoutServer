package com.github.zimablue.devoutserver.server

import com.github.zimablue.devoutserver.DevoutServer
import net.kyori.adventure.text.Component
import net.minestom.server.command.builder.Command

object ServerCommand : Command("stop") {
    init {
        setDefaultExecutor{ sender,context ->
            sender.sendMessage(Component.text("Server Stopping"))
            DevoutServer.shutdown()
        }
    }

}