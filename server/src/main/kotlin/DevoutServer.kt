import com.github.zimablue.devoutserver.api.plugin.manager.PluginManager
import com.github.zimablue.devoutserver.internal.manager.PluginManagerImpl
import net.minestom.server.MinecraftServer
import taboolib.module.database.Database
import java.net.InetSocketAddress
import java.net.SocketAddress

object DevoutServer {

    val pluginManager: PluginManager = PluginManagerImpl

    val server = MinecraftServer.init()

    val LOGGER = MinecraftServer.LOGGER
    
    init {
        pluginManager.init(MinecraftServer.process())
        MinecraftServer.getSchedulerManager().buildShutdownTask { pluginManager.shutdown() }

        pluginManager.start()
        pluginManager.gotoPreInit()
        Database.settingsFile = TODO()
    }

    fun start(address: String, port: Int) {
        start(InetSocketAddress(address, port))
    }

    fun start(address: SocketAddress) {
        pluginManager.gotoInit()
        this.server.start(address)
        pluginManager.gotoPostInit()
    }

    fun shutdown() {
        pluginManager.shutdown()
    }
}