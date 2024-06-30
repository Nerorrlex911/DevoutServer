package com.github.zimablue.devoutserver.internal.manager

import com.github.zimablue.devoutserver.api.plugin.DiscoveredPlugin
import com.github.zimablue.devoutserver.api.plugin.Plugin
import com.github.zimablue.devoutserver.api.plugin.manager.PluginManager
import com.google.gson.Gson
import net.minestom.dependencies.DependencyGetter
import net.minestom.dependencies.ResolvedDependency
import net.minestom.dependencies.maven.MavenRepository
import net.minestom.server.ServerProcess

import net.minestom.server.utils.validate.Check
import org.slf4j.LoggerFactory
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.zip.ZipFile

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

    fun loadPlugins() {


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
        // go through all the discovered plugins and get their dependencies as plugins
        allPlugins@ for (discoveredPlugin in discoveredPlugins) {
            val dependencies = mutableListOf<DiscoveredPlugin>()
            // Map the dependencies into DiscoveredPlugins.
            for (dependency in discoveredPlugin.dependencies) {
                val dependent = discoveredPlugins.find { it.name == dependency }
                // Specifies an plugin we don't have.
                if (dependent == null) {
                    // attempt to see if it is not already loaded (happens with dynamic (re)loading)
                    if(containsKey(dependency.lowercase(Locale.getDefault()))) {
                        dependencies.add(get(dependency.lowercase(Locale.getDefault()))!!.origin)
                    }
                    continue// Go to the next loop in this dependency loop, this iteration is done.
                } else {
                    // dependency isn't loaded, move on.
                    LOGGER.error("Plugin {} requires an plugin called {}.", discoveredPlugin.name, dependency)
                    LOGGER.error("However the plugin {} could not be found.", dependency)
                    LOGGER.error("Therefore {} will not be loaded.", discoveredPlugin.name)
                    discoveredPlugin.loadStatus = DiscoveredPlugin.LoadStatus.MISSING_DEPENDENCIES
                    continue@allPlugins // the above labeled loop will go to the next plugin as this dependency is invalid.
                }
            }
            dependencyMap[discoveredPlugin] = dependencies
        }
        val orderedPlugins = mutableListOf<DiscoveredPlugin>()
        var loadablePlugins: Map<DiscoveredPlugin, MutableList<DiscoveredPlugin>>?
        //每轮循环中寻找入度为0的插件，将其加入有序列表中，并将其从依赖图中移除，直到再也没有入度为0的插件
        while ((dependencyMap.filterValues {
            plugins -> plugins.isEmpty() || plugins.any { !containsKey(it.name!!.lowercase(Locale.getDefault())) }
        }.apply { loadablePlugins = this }).isNotEmpty()) {
            for (plugin in loadablePlugins!!.keys) {
                orderedPlugins.add(plugin)
                dependencyMap.remove(plugin)
                dependencyMap.forEach { (_, value) -> value.remove(plugin) }
            }
        }
        // Check if there are cyclic plugins.
        if (dependencyMap.isNotEmpty()) {
            LOGGER.error("Minestom found {} cyclic plugins.", dependencyMap.size)
            LOGGER.error("Cyclic plugins depend on each other and can therefore not be loaded.")
            for ((discoveredPlugin, value) in dependencyMap) {
                LOGGER.error("{} could not be loaded, as it depends on: {}.",
                    discoveredPlugin.name,
                    value.map { obj -> obj.name }.joinToString(", ")
                )
            }
        }
        return orderedPlugins

    }

    fun loadDependencies(plugins: List<DiscoveredPlugin>) {
        val allLoadedPlugins: MutableList<DiscoveredPlugin> = LinkedList(plugins)

        for (plugin in values) allLoadedPlugins.add(plugin.origin)

        for (discoveredPlugin in plugins) {
            try {
                val getter = DependencyGetter()
                val externalDependencies = discoveredPlugin.externalDependencies?:continue
                val repoList: MutableList<MavenRepository> = LinkedList<MavenRepository>()
                for (repository in externalDependencies.repositories) {
                    check(!repository.name.isNullOrEmpty()) { "Missing 'name' element in repository object." }

                    check(!repository.url.isNullOrEmpty()) { "Missing 'url' element in repository object." }

                    repoList.add(MavenRepository(repository.name, repository.url))
                }

                getter.addMavenResolver(repoList)

                for (artifact in externalDependencies.artifacts) {
                    val resolved =
                        getter.get(artifact, dependenciesFolder)
                    addDependencyFile(resolved, discoveredPlugin)
                    LOGGER.trace("Dependency of plugin {}: {}", discoveredPlugin.name, resolved)
                }

                val pluginClassLoader = discoveredPlugin.classLoader!!
                for (dependencyName in discoveredPlugin.dependencies) {
                    val resolved = plugins.stream()
                        .filter { ext: DiscoveredPlugin ->
                            ext.name.equals(dependencyName, ignoreCase = true)
                        }
                        .findFirst()
                        .orElseThrow {
                            java.lang.IllegalStateException(
                                "Unknown dependency '" + dependencyName + "' of '" + discoveredPlugin.name + "'"
                            )
                        }

                    val dependencyClassLoader = resolved.classLoader!!

                    pluginClassLoader.addChild(dependencyClassLoader)
                    LOGGER.trace("Dependency of plugin {}: {}", discoveredPlugin.name, resolved)
                }
            } catch (e: java.lang.Exception) {
                discoveredPlugin.loadStatus = DiscoveredPlugin.LoadStatus.MISSING_DEPENDENCIES
                LOGGER.error("Failed to load dependencies for plugin {}", discoveredPlugin.name)
                LOGGER.error("Plugin '{}' will not be loaded", discoveredPlugin.name)
                LOGGER.error("This is the exception", e)
            }
        }
    }
    private fun addDependencyFile(dependency: ResolvedDependency, plugin: DiscoveredPlugin) {
        val location = dependency.contentsLocation
        plugin.files.add(location)
        plugin.classLoader!!.addURL(location)
        LOGGER.trace(
            "Added dependency {} to plugin {} classpath",
            location.toExternalForm(),
            plugin.name
        )

        // recurse to add full dependency tree
        if (dependency.subdependencies.isNotEmpty()) {
            LOGGER.trace("Dependency {} has subdependencies, adding...", location.toExternalForm())
            for (sub in dependency.subdependencies) {
                addDependencyFile(sub, plugin)
            }
            LOGGER.trace("Dependency {} has had its subdependencies added.", location.toExternalForm())
        }
    }

    fun loadPlugin(discoveredPlugin: DiscoveredPlugin): Plugin? {
        // Create Plugin (authors, version etc.)
        val pluginName = discoveredPlugin.name!!
        val mainClass = discoveredPlugin.entrypoint

        val loader = discoveredPlugin.classLoader

        if (containsKey(pluginName.lowercase(Locale.getDefault()))) {
            LOGGER.error("An plugin called '{}' has already been registered.", pluginName)
            return null
        }

        val jarClass: Class<*>
        try {
            jarClass = Class.forName(mainClass, true, loader)
        } catch (e: ClassNotFoundException) {
            LOGGER.error(
                "Could not find main class '{}' in plugin '{}'.",
                mainClass, pluginName, e
            )
            return null
        }

        val pluginClass: Class<out Plugin>
        try {
            pluginClass = jarClass.asSubclass(Plugin::class.java)
        } catch (e: ClassCastException) {
            LOGGER.error(
                "Main class '{}' in '{}' does not extend the 'Plugin' superclass.",
                mainClass,
                pluginName,
                e
            )
            return null
        }

        val constructor: Constructor<out Plugin>
        try {
            constructor = pluginClass.getDeclaredConstructor()
            // Let's just make it accessible, plugin creators don't have to make this public.
            constructor.setAccessible(true)
        } catch (e: NoSuchMethodException) {
            LOGGER.error(
                "Main class '{}' in '{}' does not define a no-args constructor.",
                mainClass,
                pluginName,
                e
            )
            return null
        }
        var plugin: Plugin? = null
        try {
            plugin = constructor.newInstance()
        } catch (e: InstantiationException) {
            LOGGER.error(
                "Main class '{}' in '{}' cannot be an abstract class.",
                mainClass,
                pluginName,
                e
            )
            return null
        } catch (ignored: IllegalAccessException) {
            // We made it accessible, should not occur
        } catch (e: InvocationTargetException) {
            LOGGER.error(
                "While instantiating the main class '{}' in '{}' an exception was thrown.",
                mainClass,
                pluginName,
                e.targetException
            )
            return null
        }

        // add dependents to pre-existing plugins, so that they can easily be found during reloading
        for (dependencyName in discoveredPlugin.dependencies) {
            val dependency = get(dependencyName.lowercase(Locale.getDefault()))
            dependency?.dependents?.add(discoveredPlugin.name!!)
                ?: LOGGER.warn(
                    "Dependency {} of {} is null? This means the plugin has been loaded without its dependency, which could cause issues later.",
                    dependencyName,
                    discoveredPlugin.name
                )
        }

        // add to a linked hash map, as they preserve order
        plugin?.let { put(pluginName.lowercase(Locale.getDefault()), it) }

        return plugin
    }
    
    fun discoverFromJar(file: File): DiscoveredPlugin? {
        try {
            ZipFile(file).use { f ->
                val entry = f.getEntry("plugin.json")
                    ?: throw IllegalStateException("Missing plugin.json in plugin " + file.name + ".")
                val reader = InputStreamReader(f.getInputStream(entry))

                // Initialize DiscoveredPlugin from JSON.
                val pluginConfig = Configuration.loadFromReader(reader,Type.JSON)
                val plugin = Configuration.deserialize<DiscoveredPlugin>(pluginConfig,true)
                plugin.originalJar = file
                plugin.files.add(file.toURI().toURL())
                plugin.dataDirectory = pluginDataRoot.resolve(plugin.name)

                // Verify integrity and ensure defaults
                DiscoveredPlugin.verifyIntegrity(plugin)
                return plugin
            }
        } catch (e: IOException) {
            serverProcess.exception().handleException(e)
            return null
        }
    }


    //
    // Shutdown / Unload
    //
    /**
     * Shutdowns all the plugins by unloading them.
     */
    override fun shutdown() { // copy names, as the plugins map will be modified via the calls to unload
        val pluginNames = HashSet(this.keys)
        for (ext in pluginNames) {
            if (this.containsKey(ext)) { // is still loaded? Because plugins can depend on one another, it might have already been unloaded
                unloadPlugin(ext)
            }
        }
    }

    private fun unloadPlugin(pluginName: String) {
        val ext = this[pluginName.lowercase(Locale.getDefault())]
            ?: throw IllegalArgumentException("Plugin $pluginName is not currently loaded.")

        val dependents = LinkedList(ext.dependents) // copy dependents list

        for (dependentID in dependents) {
            val dependentExt = this[dependentID.lowercase(Locale.getDefault())]!!
            // check if plugin isn't already unloaded.
            LOGGER.info(
                "Unloading dependent plugin {} (because it depends on {})",
                dependentID,
                pluginName
            )
            unload(dependentExt)
        }

        LOGGER.info("Unloading plugin {}", pluginName)
        unload(ext)
    }

    private fun unload(ext: Plugin) {
        ext.preTerminate()
        ext.terminate()

        ext.pluginClassLoader.terminate()

        ext.postTerminate()

        // remove from loaded plugins
        val id = ext.origin.name!!.lowercase(Locale.getDefault())
        this.remove(id)

        // cleanup classloader
        // TODO: Is it necessary to remove the CLs since this is only called on shutdown?
    }

    override fun start() {
        if (state == State.DO_NOT_START) {
            LOGGER.warn("Plugin loadOnStartup option is set to false, plugins are therefore neither loaded or initialized.")
            return
        }
        Check.stateCondition(state != State.NOT_STARTED, "PluginManager has already been started")
        loadPlugins()
        state = State.STARTED
    }

    override fun gotoPreInit() {
        if (state == State.DO_NOT_START) return
        Check.stateCondition(state != State.STARTED, "Plugins have already done pre initialization")
        values.forEach{it.preInitialize()}
        state = State.PRE_INIT
    }

    override fun gotoInit() {
        if (state == State.DO_NOT_START) return
        Check.stateCondition(state != State.PRE_INIT, "Plugins have already done initialization")
        values.forEach { it.initialize() }
        state = State.INIT
    }

    override fun gotoPostInit() {
        if (state == State.DO_NOT_START) return
        Check.stateCondition(state != State.INIT, "Plugins have already done post initialization")
        values.forEach { it.postInitialize() }
        state = State.POST_INIT
    }
}