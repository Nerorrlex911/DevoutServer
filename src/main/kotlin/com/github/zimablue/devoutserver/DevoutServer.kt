package com.github.zimablue.devoutserver

import com.github.zimablue.devoutserver.server.config.ConfigManagerImpl
import com.github.zimablue.devoutserver.server.lifecycle.LifeCycle
import com.github.zimablue.devoutserver.server.lifecycle.LifeCycleManagerImpl
import com.github.zimablue.devoutserver.plugin.PluginManagerImpl
import com.github.zimablue.devoutserver.server.terminal.EasyTerminal
import net.minestom.server.MinecraftServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.file.Path
import java.nio.file.Paths

object DevoutServer {

    val currentDir: Path = Paths.get("").toAbsolutePath()


    val server: MinecraftServer by lazy { MinecraftServer.init() }


    val LOGGER: Logger by lazy { LoggerFactory.getLogger(DevoutServer::class.java) }
    
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
        PluginManagerImpl.gotoInit()
        server.start(address)
        PluginManagerImpl.gotoPostInit()
    }

    fun shutdown() {
        PluginManagerImpl.shutdown()
        LifeCycleManagerImpl.lifeCycle(LifeCycle.SHUTDOWN)
        EasyTerminal.stop()
        MinecraftServer.stopCleanly()
    }
}