package com.github.zimablue.devoutserver.server.command

import com.github.zimablue.devoutserver.DevoutServer.scriptManager
import com.github.zimablue.devoutserver.feature.lamp.LuckPermission
import com.github.zimablue.devoutserver.server.command.annotation.RegLamp
import revxrsal.commands.annotation.Command
import revxrsal.commands.annotation.Description
import revxrsal.commands.annotation.Subcommand
import revxrsal.commands.minestom.actor.MinestomCommandActor

@RegLamp
@Command("script")
class ScriptCommand {

    @LuckPermission("devoutserver.command.script.run")
    @Subcommand("run")
    @Description("Run a script in server core")
    fun run(actor: MinestomCommandActor, path: String, function: String, args: Array<String>) {
        scriptManager.run(path, function, mapOf("sender" to actor.sender()), *args)
    }

    @LuckPermission("devoutserver.command.script.eval")
    @Subcommand("eval")
    @Description("Eval a script string with the public scriptEngine in server core")
    fun eval(actor: MinestomCommandActor, script: String) {
        scriptManager.eval(script)
    }
    @LuckPermission("devoutserver.command.script.reload")
    @Subcommand("reload")
    @Description("reload all scripts in server core")
    fun reload(actor: MinestomCommandActor) {
        scriptManager.reload()
        actor.sender().sendMessage("scripts reloaded")
    }
}