package com.github.zimablue.devoutserver.api.plugin

import com.github.zimablue.devoutserver.api.map.component.Keyable
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

abstract class Plugin protected constructor() : Keyable<String> {

    /**
     * @return A modifiable list of dependents.
     */
    /**
     * List of extensions that depend on this extension.
     */
    val dependents = HashSet<String>()


    open fun onLoad() {

    }

    open fun onEnable() {

    }

    open fun onDisable() {

    }

    fun preInitialize() {
    }

    abstract fun initialize()

    fun postInitialize() {
    }

    fun preTerminate() {
    }

    abstract fun terminate()

    fun postTerminate() {
    }
    val pluginClassLoader: PluginClassLoader
        get() {
            val classLoader = javaClass.classLoader
            if (classLoader is PluginClassLoader) {
                return classLoader
            }
            throw IllegalStateException("Plugin class loader is not an PluginClassLoader")
        }


    val origin: DiscoveredPlugin
        get() = pluginClassLoader.discoveredPlugin

    val logger: ComponentLogger
        /**
         * Gets the logger for the extension
         *
         * @return The logger for the extension
         */
        get() = pluginClassLoader.logger

    val eventNode: EventNode<Event>
        get() = pluginClassLoader.eventNode

    val dataDirectory: Path
        get() = origin.dataDirectory!!

    /**
     * Gets a resource from the extension directory, or from inside the jar if it does not
     * exist in the extension directory.
     *
     *
     * If it does not exist in the extension directory, it will be copied from inside the jar.
     *
     *
     * The caller is responsible for closing the returned [InputStream].
     *
     * @param fileName The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    fun getResource(fileName: String): InputStream? {
        return getResource(Paths.get(fileName))
    }

    /**
     * Gets a resource from the extension directory, or from inside the jar if it does not
     * exist in the extension directory.
     *
     *
     * If it does not exist in the extension directory, it will be copied from inside the jar.
     *
     *
     * The caller is responsible for closing the returned [InputStream].
     *
     * @param target The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    fun getResource(target: Path): InputStream? {
        val targetFile = dataDirectory.resolve(target)
        try {
            // Copy from jar if the file does not exist in the extension data directory.
            if (!Files.exists(targetFile)) {
                savePackagedResource(target)
            }

            return Files.newInputStream(targetFile)
        } catch (ex: IOException) {
            logger.info("Failed to read resource {}.", target, ex)
            return null
        }
    }

    /**
     * Gets a resource from inside the extension jar.
     *
     *
     * The caller is responsible for closing the returned [InputStream].
     *
     * @param fileName The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    fun getPackagedResource(fileName: String): InputStream? {
        try {
            val url = origin.classLoader!!.getResource(fileName)
            if (url == null) {
                logger.debug("Resource not found: {}", fileName)
                return null
            }

            return url.openConnection().getInputStream()
        } catch (ex: IOException) {
            logger.debug("Failed to load resource {}.", fileName, ex)
            return null
        }
    }

    /**
     * Gets a resource from inside the extension jar.
     *
     *
     * The caller is responsible for closing the returned [InputStream].
     *
     * @param target The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    fun getPackagedResource(target: Path): InputStream? {
        return getPackagedResource(target.toString().replace('\\', '/'))
    }

    /**
     * Copies a resource file to the extension directory, replacing any existing copy.
     *
     * @param fileName The resource to save
     * @return True if the resource was saved successfully, null otherwise
     */
    fun savePackagedResource(fileName: String): Boolean {
        return savePackagedResource(Paths.get(fileName))
    }

    /**
     * Copies a resource file to the extension directory, replacing any existing copy.
     *
     * @param target The resource to save
     * @return True if the resource was saved successfully, null otherwise
     */
    fun savePackagedResource(target: Path): Boolean {
        val targetFile = dataDirectory.resolve(target)
        try {
            getPackagedResource(target).use { `is` ->
                if (`is` == null) {
                    return false
                }
                Files.createDirectories(targetFile.parent)
                Files.copy(`is`, targetFile, StandardCopyOption.REPLACE_EXISTING)
                return true
            }
        } catch (ex: IOException) {
            logger.debug("Failed to save resource {}.", target, ex)
            return false
        }
    }
}