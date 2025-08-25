package com.github.zimablue.devoutserver.plugin


import com.github.zimablue.devoutserver.lifecycle.LifeCycle
import com.github.zimablue.devoutserver.lifecycle.LifeCycleManagerImpl.lifeCycle
import com.github.zimablue.devoutserver.plugin.lifecycle.PluginLifeCycle
import com.github.zimablue.devoutserver.util.ClassUtil.instance
import com.github.zimablue.devoutserver.util.ClassUtil.isSingleton
import com.google.gson.Gson
import net.minestom.dependencies.DependencyGetter
import net.minestom.dependencies.ResolvedDependency
import net.minestom.dependencies.maven.MavenRepository
import net.minestom.server.ServerProcess
import net.minestom.server.utils.validate.Check
import org.slf4j.LoggerFactory
import org.tabooproject.reflex.FastInstGetter
import taboolib.module.configuration.Configuration
import java.io.File
import java.io.IOException
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.zip.ZipFile

object PluginManagerImpl : PluginManager() {

    val LOGGER by lazy { LoggerFactory.getLogger("PluginManager") }
    val INDEV_CLASSES_FOLDER = "minestom.plugin.indevfolder.classes"
    val INDEV_RESOURCES_FOLDER = "minestom.plugin.indevfolder.resources"
    val GSON: Gson = Gson()
    lateinit var serverProcess: ServerProcess

    val pluginFolder by lazy { File("plugins") }
    private val dependenciesFolder by lazy { File(pluginFolder, ".libs") }
    val pluginDataRoot by lazy { pluginFolder.toPath() }

    private enum class State {
        DO_NOT_START, NOT_STARTED, STARTED, PRE_INIT, INIT, POST_INIT
    }

    private var state = State.NOT_STARTED

    override fun init(serverProcess: ServerProcess) {
        PluginManagerImpl.serverProcess = serverProcess
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
        state = if (loadOnStartup) State.NOT_STARTED else State.DO_NOT_START
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

        //discover plugins
        val discoveredPlugins = discoverPlugins()
        if (discoveredPlugins.isEmpty()) {
            LOGGER.info("No plugins found")
            return
        }

        //create classloader for plugins
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
        //generate load order
        val orderedPlugins = generateLoadOrder(discoveredPlugins)
        //load dependencies
        loadDependencies(orderedPlugins)
        orderedPlugins.removeIf { it.loadStatus != DiscoveredPlugin.LoadStatus.LOAD_SUCCESS }
        //load plugins (DiscoveredPlugin to Plugin class)
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
            if (file.isDirectory) continue
            // Ignore non .jar files
            if (!file.name.endsWith(".jar")) continue
            val plugin = discoverFromJar(file)
            if (plugin != null && plugin.loadStatus == DiscoveredPlugin.LoadStatus.LOAD_SUCCESS) {
                plugins.add(plugin)
            }
        }
        return plugins
    }

    fun generateLoadOrder(discoveredPlugins: MutableList<DiscoveredPlugin>): MutableList<DiscoveredPlugin> {
        val dependencyMap = mutableMapOf<DiscoveredPlugin, MutableList<DiscoveredPlugin>>()
        val visited = mutableSetOf<DiscoveredPlugin>()
        val order = mutableListOf<DiscoveredPlugin>()

        for (plugin in discoveredPlugins) {
            if (!visited.contains(plugin)) {
                dfs(plugin, visited, order, dependencyMap, discoveredPlugins)
            }
        }

        if (order.size < discoveredPlugins.size) {
            LOGGER.error("Minestom found ${discoveredPlugins.size - order.size} cyclic plugins.")
            LOGGER.error("Cyclic plugins depend on each other and can therefore not be loaded.")
            for (plugin in discoveredPlugins) {
                if (!visited.contains(plugin)) {
                    LOGGER.error("${plugin.name} could not be loaded, as it depends on cyclic dependencies.")
                }
            }
        }

        return order.reversed().toMutableList()
    }

    private fun dfs(
        plugin: DiscoveredPlugin,
        visited: MutableSet<DiscoveredPlugin>,
        order: MutableList<DiscoveredPlugin>,
        dependencyMap: MutableMap<DiscoveredPlugin, MutableList<DiscoveredPlugin>>,
        discoveredPlugins: List<DiscoveredPlugin>
    ) {
        visited.add(plugin)

        val dependenciesToProcess = listOf(
            plugin.dependencies,
            plugin.loadBefore,
            plugin.softDependencies.filter { dependency ->
                discoveredPlugins.any { plugin -> plugin.name == dependency }
            }
        )

        for (dependencies in dependenciesToProcess) {
            for (dependencyName in dependencies) {
                val dependent = discoveredPlugins.find { it.name == dependencyName }
                dependent?.let {
                    if (!visited.contains(it)) {
                        dfs(it, visited, order, dependencyMap, discoveredPlugins)
                    }
                    dependencyMap.computeIfAbsent(plugin) { mutableListOf() }.add(it)
                }
            }
        }

        order.add(plugin)
    }

    fun loadDependencies(plugins: List<DiscoveredPlugin>) {
        val allLoadedPlugins: MutableList<DiscoveredPlugin> = LinkedList(plugins)

        for (plugin in values) allLoadedPlugins.add(plugin.origin)

        for (discoveredPlugin in plugins) {
            try {
                val getter = DependencyGetter()
                val externalDependencies = discoveredPlugin.externalDependencies ?: continue
                val repoList: MutableList<MavenRepository> = LinkedList<MavenRepository>()
                for (repository in externalDependencies.repositories) {
                    check(repository.name.isNotEmpty()) { "Missing 'name' element in repository object." }
                    check(repository.url.isNotEmpty()) { "Missing 'url' element in repository object." }
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
        val pluginName = discoveredPlugin.name
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
        // Check if the plugin class is a kotlin object
        var plugin: Plugin? = null
        if(pluginClass.isSingleton()) {
            plugin = pluginClass.instance as Plugin
        }

        if (plugin == null) {
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
        }

        // add dependents to pre-existing plugins, so that they can easily be found during reloading
        for (dependencyName in discoveredPlugin.dependencies) {
            val dependency = get(dependencyName.lowercase(Locale.getDefault()))
            dependency?.dependents?.add(discoveredPlugin.name)
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
                val entry = f.getEntry("plugin.yml")
                    ?: throw IllegalStateException("Missing plugin.yml in plugin " + file.name + ".")
                val pluginConfig = Configuration.loadFromInputStream(f.getInputStream(entry))
                // Initialize DiscoveredPlugin from yml config.
                val plugin = Configuration.deserialize<DiscoveredPlugin>(pluginConfig,false)
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

    fun reload(plugin: Plugin) {
        LOGGER.info("Reloading plugin: ${plugin.name}")
        plugin.onReload()
        plugin.lifeCycleManager.lifeCycle(PluginLifeCycle.RELOAD)
    }


    //
    // Shutdown / Unload
    //
    /**
     * Shutdowns all the plugins by unloading them.
     */
    override fun shutdown() { // copy names, as the plugins map will be modified via the calls to unload
        val pluginNames = HashSet(this.keys)
        lifeCycle(LifeCycle.DISABLE)
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

    private fun unload(plugin: Plugin) {
        plugin.preTerminate()
        plugin.onDisable()
        plugin.lifeCycleManager.lifeCycle(PluginLifeCycle.DISABLE)

        plugin.pluginClassLoader.terminate()

        plugin.postTerminate()

        // remove from loaded plugins
        val id = plugin.origin.name.lowercase(Locale.getDefault())
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
        lifeCycle(LifeCycle.LOAD)
        values.forEach{
            it.onLoad()
            it.lifeCycleManager.lifeCycle(PluginLifeCycle.LOAD)
        }
        state = State.PRE_INIT
    }

    override fun gotoInit() {
        if (state == State.DO_NOT_START) return
        Check.stateCondition(state != State.PRE_INIT, "Plugins have already done initialization")
        lifeCycle(LifeCycle.ENABLE)
        values.forEach {
            it.onEnable()
            it.lifeCycleManager.lifeCycle(PluginLifeCycle.ENABLE)
        }
        state = State.INIT
    }

    override fun gotoPostInit() {
        if (state == State.DO_NOT_START) return
        Check.stateCondition(state != State.INIT, "Plugins have already done post initialization")
        lifeCycle(LifeCycle.ACTIVE)
        values.forEach {
            it.onActive()
            it.lifeCycleManager.lifeCycle(PluginLifeCycle.ACTIVE)
        }
        state = State.POST_INIT
    }
}