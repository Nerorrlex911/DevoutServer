package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.feature.lamp.LuckPermission
import com.github.zimablue.devoutserver.server.command.annotation.RegLamp
import com.github.zimablue.devoutserver.util.colored
import net.kyori.adventure.text.Component
import net.minestom.server.component.DataComponents
import net.minestom.server.instance.Instance
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Optional
import revxrsal.commands.minestom.actor.MinestomCommandActor

@RegLamp
class StatisticCommand {
    @LuckPermission("devoutserver.command.statistic")
    @Command("statistic instance <inst>")
    @Description("Usage: /statistic instance [<inst>]")
    fun statisticInstance(actor: MinestomCommandActor,@Optional inst: Instance?=actor.asPlayer()?.instance) {
        val sender = actor.sender()
        if(inst == null) {
            sender.sendMessage(Component.text("No instance found!"))
            return
        }
        val statistics = mutableMapOf(
            "uuid" to inst.uuid.toString(),
            "players" to inst.players.joinToString(",") { it.username },
            "entities amount" to inst.entities.count().toString(),
        )
        sender.sendMessage("§6Instance Statistics:".colored())
        statistics.forEach { (key, value) ->
            sender.sendMessage("§e$key: §f$value".colored())
        }
        inst.entities.forEach {
            val name = it[DataComponents.CUSTOM_NAME]
            val type = it.entityType.name()
            val pos = it.position
            sender.sendMessage("entity: ${it.uuid}")
            sender.sendMessage(" - name: ${name?.toString() ?: "null"}")
            sender.sendMessage(" - type: $type")
            sender.sendMessage(" - pos: $pos")
        }
    }
}