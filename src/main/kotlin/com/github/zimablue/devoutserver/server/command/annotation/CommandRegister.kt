package com.github.zimablue.devoutserver.server.command.annotation

import com.github.zimablue.devoutserver.annotation.AnnotationManagerImpl
import com.github.zimablue.devoutserver.lifecycle.Awake
import com.github.zimablue.devoutserver.lifecycle.LifeCycle
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import org.tabooproject.reflex.FastInstGetter

object CommandRegister {
    @Awake(LifeCycle.NONE)
    fun registerCommands() {
        val regCommandClasses = AnnotationManagerImpl.getTargets<RegCommand>().third
        for (commandClass in regCommandClasses) {
            val commandInstance = FastInstGetter(commandClass.name).instance
            MinecraftServer.getCommandManager().register(commandInstance as Command)
        }
    }
}