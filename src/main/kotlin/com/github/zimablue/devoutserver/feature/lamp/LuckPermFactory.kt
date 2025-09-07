package com.github.zimablue.devoutserver.feature.lamp

import com.github.zimablue.devoutserver.feature.luckperms.LuckPerms.hasPermission
import revxrsal.commands.Lamp
import revxrsal.commands.annotation.list.AnnotationList
import revxrsal.commands.command.CommandPermission
import revxrsal.commands.minestom.actor.MinestomCommandActor

class LuckPermFactory : CommandPermission.Factory<MinestomCommandActor> {
    override fun create(annotations: AnnotationList, lamp: Lamp<MinestomCommandActor>): CommandPermission<MinestomCommandActor> {
        return CommandPermission {
            it.sender().hasPermission(annotations.get(LuckPermission::class.java)?.value?:return@CommandPermission true)
        }
    }
}