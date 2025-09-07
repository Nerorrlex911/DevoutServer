package com.github.zimablue.devoutserver

import com.github.zimablue.devoutserver.feature.lamp.LuckPermFactory
import com.github.zimablue.devoutserver.plugin.PluginManagerImpl
import com.github.zimablue.devoutserver.script.ScriptManager
import com.github.zimablue.devoutserver.script.ScriptManagerImpl
import com.github.zimablue.devoutserver.server.command.ScriptCommand
import com.github.zimablue.devoutserver.server.config.ConfigManagerImpl
import com.github.zimablue.devoutserver.server.lifecycle.LifeCycle
import com.github.zimablue.devoutserver.server.lifecycle.LifeCycleManagerImpl
import com.github.zimablue.devoutserver.server.terminal.EasyTerminal
import net.minestom.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import revxrsal.commands.Lamp
import revxrsal.commands.minestom.MinestomLamp
import revxrsal.commands.minestom.actor.MinestomCommandActor
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

object DevoutServer {

    val currentDir: Path = Paths.get("").toAbsolutePath()

    val lamp: Lamp<MinestomCommandActor> = MinestomLamp.builder()
        .permissionFactory(LuckPermFactory())
        .build()

    val scriptManager: ScriptManager = ScriptManagerImpl


    val server: MinecraftServer by lazy { MinecraftServer.init() }


    val LOGGER: Logger by lazy { LoggerFactory.getLogger(DevoutServer::class.java) }
    
    init {
        server
        PluginManagerImpl.init(MinecraftServer.process())
        MinecraftServer.getSchedulerManager().buildShutdownTask { EasyTerminal.stop() }

        PluginManagerImpl.start()
        PluginManagerImpl.gotoPreInit()
    }

    fun start() {
        start(InetSocketAddress(ConfigManagerImpl.serverConfig.address, ConfigManagerImpl.serverConfig.port))
    }

    fun start(address: SocketAddress) {
        PluginManagerImpl.gotoInit()
        server.start(address)
        LOGGER.info("registered commands: ${MinecraftServer.getCommandManager().commands.map { it.name }}")
        PluginManagerImpl.gotoPostInit()
    }

    fun shutdown() {
        PluginManagerImpl.shutdown()
        LifeCycleManagerImpl.lifeCycle(LifeCycle.SHUTDOWN)
        MinecraftServer.stopCleanly()
        exitProcess(0)
    }
}