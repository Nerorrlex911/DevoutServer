package com.github.zimablue.devoutserver.internal.manager

import com.github.zimablue.devoutserver.api.plugin.DiscoveredPlugin
import com.github.zimablue.devoutserver.api.plugin.Plugin
import com.github.zimablue.devoutserver.api.plugin.manager.PluginManager
import com.google.gson.Gson
import net.minestom.server.ServerProcess
import net.minestom.server.utils.validate.Check
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import java.util.zip.ZipFile
import kotlin.collections.HashMap

object PluginManagerImpl : PluginManager() {

    val LOGGER = LoggerFactory.getLogger(PluginManagerImpl::class.java)
    val INDEV_CLASSES_FOLDER = "minestom.plugin.indevfolder.classes"
    val INDEV_RESOURCES_FOLDER = "minestom.plugin.indevfolder.resources"
    val GSON: Gson = Gson()
    lateinit var serverProcess: ServerProcess

    val pluginFolder = File(System.getProperty("minestom.plugin.folder", "plugins"))
    private val dependenciesFolder = File(pluginFolder, ".libs")
    var pluginDataRoot = pluginFolder.toPath()

    private enum class State {
        DO_NOT_START, NOT_STARTED, STARTED, PRE_INIT, INIT, POST_INIT
    }

    private var state = State.NOT_STARTED

    override fun init(serverProcess: ServerProcess) {
        this.serverProcess = serverProcess
    }

    /**
     * Gets if the plugins should be loaded during startup.
     *
     *
     * Default value is 'true'.
     *
     * @return true if plugins are loaded in [net.minestom.server.MinecraftServer.start]
     */
    fun shouldLoadOnStartup(): Boolean {
        return state != State.DO_NOT_START
    }

    /**
     * Used to specify if you want plugins to be loaded and initialized during startup.
     *
     *
     * Only useful before the server start.
     *
     * @param loadOnStartup true to load plugins on startup, false to do nothing
     */
    fun setLoadOnStartup(loadOnStartup: Boolean) {
        Check.stateCondition(
            state.ordinal > State.NOT_STARTED.ordinal,
            "Plugins have already been initialized"
        )
        this.state = if (loadOnStartup) State.NOT_STARTED else State.DO_NOT_START
    }

    fun getPlugins(): Collection<Plugin> {
        return values
    }

    fun loadExtensions() {


        // Make plugins folder if necessary
        if (!pluginFolder.exists()) {
            if (!pluginFolder.mkdirs()) {
                LOGGER.error("Could not find or create the plugin folder, plugins will not be loaded!")
                return
            }
        }
        // Make dependencies folder if necessary
        if (!dependenciesFolder.exists()) {
            if (!dependenciesFolder.mkdirs()) {
                LOGGER.error("Could not find nor create the plugin dependencies folder, plugins will not be loaded!")
                return
            }
        }

        val discoveredPlugins = discoverPlugins()
        if (discoveredPlugins.isEmpty()) {
            LOGGER.info("No plugins found")
            return
        }
        val pluginIterator = discoveredPlugins.iterator()
        while (pluginIterator.hasNext()) {
            val discoveredPlugin = pluginIterator.next()
            try {
                discoveredPlugin.createClassLoader()
            } catch (e: Exception) {
                discoveredPlugin.loadStatus = DiscoveredPlugin.LoadStatus.FAILED_TO_SETUP_CLASSLOADER
                serverProcess.exception().handleException(e)
                LOGGER.error("Failed to create class loader for plugin: ${discoveredPlugin.name}", e)
                pluginIterator.remove()
            }
        }
        val orderedPlugins = generateLoadOrder(discoveredPlugins)
        loadDependencies(orderedPlugins)
        orderedPlugins.removeIf { it.loadStatus != DiscoveredPlugin.LoadStatus.LOAD_SUCCESS }

        orderedPlugins.forEach {
            try {
                loadPlugin(it)
            } catch (e: Exception) {
                it.loadStatus = DiscoveredPlugin.LoadStatus.LOAD_FAILED
                serverProcess.exception().handleException(e)
                LOGGER.error("Failed to load plugin: ${it.name}", e)
            }
        }


    }

    fun discoverPlugins(): MutableList<DiscoveredPlugin> {
        val plugins = LinkedList<DiscoveredPlugin>()

        val fileList = pluginFolder.listFiles()?:return mutableListOf()

        for (file in fileList) {
            // Ignore folders

            if (file.isDirectory) {
                continue
            }

            // Ignore non .jar files
            if (!file.name.endsWith(".jar")) {
                continue
            }

            val plugin = discoverFromJar(file)
            if (plugin != null && plugin.loadStatus == DiscoveredPlugin.LoadStatus.LOAD_SUCCESS) {
                plugins.add(plugin)
            }
        }
        return plugins
    }

    fun generateLoadOrder(discoveredPlugins: MutableList<DiscoveredPlugin>): MutableList<DiscoveredPlugin> {
        val dependencyMap = HashMap<DiscoveredPlugin,MutableList<DiscoveredPlugin>>()
        TODO()
    }

    fun loadDependencies(discoveredPlugins: MutableList<DiscoveredPlugin>) {
        TODO()
    }

    fun loadPlugin(discoveredPlugin: DiscoveredPlugin) {
        TODO()
    }
    
    fun discoverFromJar(file: File): DiscoveredPlugin? {
        try {
            ZipFile(file).use { f ->
                val entry = f.getEntry("plugin.json")
                    ?: throw IllegalStateException("Missing plugin.json in plugin " + file.name + ".")
                val reader = InputStreamReader(f.getInputStream(entry))

                // Initialize DiscoveredExtension from GSON.
                val plugin = GSON.fromJson(
                    reader,
                    DiscoveredPlugin::class.java
                )
                plugin.originalJar = file
                plugin.files.add(file.toURI().toURL())
                plugin.dataDirectory = pluginDataRoot.resolve(plugin.name!!)

                // Verify integrity and ensure defaults
                DiscoveredPlugin.verifyIntegrity(plugin)
                return plugin
            }
        } catch (e: IOException) {
            serverProcess.exception().handleException(e)
            return null
        }
    }

    override fun shutdown() {
        TODO()
    }

    override fun start() {
        if (state == State.DO_NOT_START) {
            LOGGER.warn("Plugin loadOnStartup option is set to false, plugins are therefore neither loaded or initialized.")
            return
        }
        Check.stateCondition(state != State.NOT_STARTED, "PluginManager has already been started")
        loadExtensions()
        state = State.STARTED
    }

    override fun gotoPreInit() {
        if (state == State.DO_NOT_START) return
        Check.stateCondition(state != State.STARTED, "Extensions have already done pre initialization")
        values.forEach{it.preInitialize()}
        state = State.PRE_INIT
    }

    override fun gotoInit() {
        if (state == State.DO_NOT_START) return
        Check.stateCondition(state != State.PRE_INIT, "Extensions have already done initialization")
        values.forEach { it.initialize() }
        state = State.INIT
    }

    override fun gotoPostInit() {
        if (state == State.DO_NOT_START) return
        Check.stateCondition(state != State.INIT, "Extensions have already done post initialization")
        values.forEach { it.postInitialize() }
        state = State.POST_INIT
    }
}