package com.github.zimablue.devoutserver

import com.github.zimablue.devoutserver.config.ConfigManagerImpl
import com.github.zimablue.devoutserver.lifecycle.LifeCycle
import com.github.zimablue.devoutserver.lifecycle.LifeCycleManagerImpl
import com.github.zimablue.devoutserver.plugin.PluginManagerImpl
import com.github.zimablue.devoutserver.terminal.EasyTerminal
import net.minestom.server.MinecraftServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.SocketAddress

object DevoutServer {



    val server: MinecraftServer by lazy { MinecraftServer.init() }


    val LOGGER by lazy { LoggerFactory.getLogger(DevoutServer::class.java) }
    
    init {
        server
        PluginManagerImpl.init(MinecraftServer.process())
        MinecraftServer.getSchedulerManager().buildShutdownTask { shutdown() }

        PluginManagerImpl.start()
        PluginManagerImpl.gotoPreInit()
    }

    fun start() {
        start(InetSocketAddress(ConfigManagerImpl.serverConfig.address, ConfigManagerImpl.serverConfig.port))
    }

    fun start(address: SocketAddress) {
        EasyTerminal.start()
        PluginManagerImpl.gotoInit()
        server.start(address)
        PluginManagerImpl.gotoPostInit()
    }

    fun shutdown() {
        PluginManagerImpl.shutdown()
        LifeCycleManagerImpl.lifeCycle(LifeCycle.SHUTDOWN)
        EasyTerminal.stop()
    }
}