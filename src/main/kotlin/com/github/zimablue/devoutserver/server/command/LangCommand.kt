package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.DevoutServer.langManager
import com.github.zimablue.devoutserver.feature.lamp.LuckPermission
import com.github.zimablue.devoutserver.server.command.annotation.RegLamp
import net.minestom.server.entity.Player
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.minestom.actor.MinestomCommandActor
@RegLamp
class LangCommand {
    @LuckPermission("devoutserver.command.lang")
    @Command("lang")
    @Description("Usage: /lang <player> <key> [args...] send lang message to player")
    fun lang(actor: MinestomCommandActor, player: Player, key: String, args: Array<String>) {
        langManager.sendLang(player, key, args)
        actor.sender().sendMessage("sent lang $key to ${player.username}")
    }
    @LuckPermission("devoutserver.command.lang")
    @Command("lang")
    @Description("Usage: /lang <key> [args...] send lang message to sender")
    fun lang(actor: MinestomCommandActor, key: String, vararg args: String) {
        val sender = actor.sender()
        langManager.sendLang(sender, key, args)
        sender.sendMessage("sent lang $key to ${sender.identity().examinableName()}")
    }
}