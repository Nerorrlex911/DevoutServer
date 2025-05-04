package com.github.zimablue.devoutserver

import com.github.zimablue.devoutserver.config.ConfigManagerImpl
import com.github.zimablue.devoutserver.plugin.PluginManager
import com.github.zimablue.devoutserver.plugin.PluginManagerImpl
import com.github.zimablue.devoutserver.script.nashorn.impl.NashornHookerImpl
import com.github.zimablue.devoutserver.terminal.EasyTerminal
import net.minestom.server.MinecraftServer
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.SocketAddress

object DevoutServer {

    val pluginManager: PluginManager = PluginManagerImpl

    val server = MinecraftServer.init()

    val nashornHooker = NashornHookerImpl()

    val LOGGER = LoggerFactory.getLogger(DevoutServer::class.java)
    
    init {
        pluginManager.init(MinecraftServer.process())
        MinecraftServer.getSchedulerManager().buildShutdownTask { shutdown() }

        pluginManager.start()
        pluginManager.gotoPreInit()
    }

    fun start() {
        start(InetSocketAddress(ConfigManagerImpl.serverConfig.address, ConfigManagerImpl.serverConfig.port))
    }

    fun start(address: SocketAddress) {
        EasyTerminal.start()
        pluginManager.gotoInit()
        server.start(address)
        pluginManager.gotoPostInit()
    }

    fun shutdown() {
        pluginManager.shutdown()
        EasyTerminal.stop()
    }
}