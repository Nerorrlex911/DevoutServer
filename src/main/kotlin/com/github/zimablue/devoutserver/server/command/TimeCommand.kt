package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.feature.lamp.LuckPermission
import com.github.zimablue.devoutserver.server.command.annotation.RegLamp
import net.minestom.server.MinecraftServer
import net.minestom.server.instance.Instance
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.minestom.actor.MinestomCommandActor
import java.util.UUID

@RegLamp
class TimeCommand {
    @LuckPermission("devoutserver.command.time")
    @Command("time")
    @Description("Usage /time <time> <instance_uuid>")
    fun time(actor: MinestomCommandActor, time: Long, @Optional instance: UUID?=null) {
        if(instance == null && !actor.isPlayer) return
        val targetInstance: Instance = instance?.let{ MinecraftServer.getInstanceManager().getInstance(it) }
            ?:actor.asPlayer()?.instance?: return
        targetInstance.time = time
    }
}