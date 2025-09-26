package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.feature.luckperms.LuckPerms.hasPermission
import com.github.zimablue.devoutserver.server.command.annotation.RegCommand
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.arguments.number.ArgumentNumber
import net.minestom.server.command.builder.condition.Conditions
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.entity.Player
import java.util.*

@RegCommand
object HealthCommand : Command("health") {
    init {
        setCondition{ sender, string ->
            return@setCondition Conditions.playerOnly(sender,string)&& sender.hasPermission("devoutserver.command.health")
        }

        setDefaultExecutor{ sender, context ->
            sender.sendMessage(Component.text("Correct usage: health set|add <number>"))
        }

        val modeArg = ArgumentType.Word("mode").from("set", "add")

        val valueArg = ArgumentType.Integer("value")

        setArgumentCallback(this::onModeError, modeArg)
        setArgumentCallback(this::onValueError, valueArg)

        addSyntax(this::sendSuggestionMessage, modeArg)
        addSyntax(this::onHealthCommand, modeArg, valueArg)
    }

    private fun onModeError(sender: CommandSender, exception: ArgumentSyntaxException) {
        sender.sendMessage(Component.text("SYNTAX ERROR: '" + exception.input + "' should be replaced by 'set' or 'add'"))
    }

    private fun onValueError(sender: CommandSender, exception: ArgumentSyntaxException) {
        val error = exception.errorCode
        val input = exception.input
        when (error) {
            ArgumentNumber.NOT_NUMBER_ERROR -> sender.sendMessage(Component.text("SYNTAX ERROR: '$input' isn't a number!"))
        }
    }

    private fun sendSuggestionMessage(sender: CommandSender, context: CommandContext) {
        sender.sendMessage(Component.text("/health " + context.get("mode") + " [Integer]"))
    }

    private fun onHealthCommand(sender: CommandSender, context: CommandContext) {
        val player = sender as Player
        val mode = context.get<String>("mode")
        val value = context.get<Int>("value")

        when (mode.lowercase(Locale.getDefault())) {
            "set" -> player.health = value.toFloat()
            "add" -> player.health += value
        }

        player.sendMessage(Component.text("You have now " + player.health + " health"))
    }
}