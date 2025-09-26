package com.github.zimablue.devoutserver

import com.github.zimablue.devoutserver.feature.lamp.EntityTypeParam
import com.github.zimablue.devoutserver.feature.lamp.LuckPermFactory
import com.github.zimablue.devoutserver.plugin.PluginManagerImpl
import com.github.zimablue.devoutserver.script.ScriptManager
import com.github.zimablue.devoutserver.script.ScriptManagerImpl
import com.github.zimablue.devoutserver.server.config.ConfigManagerImpl
import com.github.zimablue.devoutserver.server.lifecycle.LifeCycle
import com.github.zimablue.devoutserver.server.lifecycle.LifeCycleManagerImpl
import com.github.zimablue.devoutserver.server.terminal.EasyTerminal
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.EntityType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import revxrsal.commands.Lamp
import revxrsal.commands.minestom.MinestomLamp
import revxrsal.commands.minestom.actor.MinestomCommandActor
import java.net.InetSocketAddress
import java.net.SocketAddress
import kotlin.system.exitProcess

object DevoutServer {


    val lamp: Lamp<MinestomCommandActor> = MinestomLamp.builder()
        .permissionFactory(LuckPermFactory())
        .parameterTypes {
            it.addParameterType(EntityType::class.java,EntityTypeParam())
        }
        .build()

    val scriptManager: ScriptManager = ScriptManagerImpl


    val server: MinecraftServer = MinecraftServer.init()


    val LOGGER: Logger by lazy { LoggerFactory.getLogger(DevoutServer::class.java) }
    
    init {
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
        PluginManagerImpl.gotoPostInit()
    }

    fun shutdown() {
        PluginManagerImpl.shutdown()
        LifeCycleManagerImpl.lifeCycle(LifeCycle.SHUTDOWN)
        MinecraftServer.stopCleanly()
        exitProcess(0)
    }
}