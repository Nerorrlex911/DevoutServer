package com.github.zimablue.devoutserver.plugin

import net.minestom.server.utils.validate.Check
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import taboolib.library.configuration.ConfigurationSection
import taboolib.module.configuration.Configuration
import java.io.File
import java.net.URL
import java.nio.file.Path
import java.util.*

/**
 * Represents an extension from an `plugin.yml` that is capable of powering an Plugin object.
 *
 */
class DiscoveredPlugin(
    /** Name of the DiscoveredPlugin. Unique for all extensions.  */
    val name: String = "",
    /** Main class of this DiscoveredPlugin, must extend Plugin.  */
    val entrypoint: String = "",
    /** Version of this extension, highly reccomended to set it.  */
    val version: String = "Unspecified",
    /** People who have made this extension.  */
    val authors: List<String> = listOf(),
    /** List of extension names that this depends on.  */
    val dependencies: MutableList<String> = mutableListOf(),
    /** List of extension names that this soft-depends on. */
    val softDependencies: MutableList<String> = mutableListOf(),
    /** List of extension names that load before this*/
    val loadBefore: MutableList<String> = mutableListOf(),
    /** List of Repositories and URLs that this depends on.  */
    val externalDependencies: ExternalDependencies = ExternalDependencies(),
    /** package for annotation scan, set it to null to disable*/
    val packageName: String? = null,
    /**
     * Extra meta on the object.
     * Do NOT use as configuration:
     *
     * Meta is meant to handle properties that will
     * be accessed by other extensions, not accessed by itself
     */
    private var meta: ConfigurationSection=Configuration.empty(),
) {


    /** All files of this extension  */
    @Transient
    val files = LinkedList<URL>()

    /** The load status of this extension -- LOAD_SUCCESS is the only good one.  */
    @Transient
    var loadStatus: LoadStatus = LoadStatus.LOAD_SUCCESS

    /** The original jar this is from.  */
    @Transient
    lateinit var originalJar: File

    @Transient
    lateinit var dataDirectory: Path

    /** The class loader that powers it.  */
    @Transient
    var classLoader: PluginClassLoader?=null

    fun createClassLoader() {
        Check.stateCondition(classLoader != null, "Plugin classloader has already been created")
        val urls = files.toTypedArray<URL>()
        classLoader = PluginClassLoader(this.name, urls,  discoveredPlugin = this)
    }

    /**
     * The status this extension has, all are breakpoints.
     *
     * LOAD_SUCCESS is the only valid one.
     */
    enum class LoadStatus(val message: String) {
        LOAD_SUCCESS("Actually, it did not fail. This message should not have been printed."),
        MISSING_DEPENDENCIES("Missing dependencies, check your logs."),
        INVALID_NAME("Invalid name."),
        NO_ENTRYPOINT("No entrypoint specified."),
        FAILED_TO_SETUP_CLASSLOADER("Plugin classloader could not be setup."),
        LOAD_FAILED("Load failed. See logs for more information."),
    }

    class Repository (
        val name: String="Repository",
        val url: String=""
    )

    class ExternalDependencies(
        val repositories: MutableList<Repository> = mutableListOf(),
        val artifacts: MutableList<String> = mutableListOf(),
    ) {

        fun isEmpty(): Boolean {
            return repositories.isEmpty() && artifacts.isEmpty()
        }
    }


    companion object {
        /** Static logger for this class.  */
        val LOGGER: Logger = LoggerFactory.getLogger(DiscoveredPlugin::class.java)

        /** The regex that this name must pass. If it doesn't, it will not be accepted.  */
        const val NAME_REGEX: String = "[A-Za-z][_A-Za-z0-9]+"

        /**
         * Ensures that all properties of this extension are properly set if they aren't
         *
         * @param extension The extension to verify
         */
        fun verifyIntegrity(extension: DiscoveredPlugin) {
            if (extension.name.isEmpty()) {
                val fileList = StringBuilder()
                for (f in extension.files) {
                    fileList.append(f.toExternalForm()).append(", ")
                }
                LOGGER.error("Plugin with no name. (at {}})", fileList)
                LOGGER.error("Plugin at ({}) will not be loaded.", fileList)
                extension.loadStatus = LoadStatus.INVALID_NAME
                return
            }

            if (!extension.name.matches(NAME_REGEX.toRegex())) {
                LOGGER.error("Plugin '{}' specified an invalid name.", extension.name)
                LOGGER.error("Plugin '{}' will not be loaded.", extension.name)
                extension.loadStatus = LoadStatus.INVALID_NAME
                return
            }

            if (extension.entrypoint.isEmpty()) {
                LOGGER.error("Plugin '{}' did not specify an entry point (via 'entrypoint').", extension.name)
                LOGGER.error("Plugin '{}' will not be loaded.", extension.name)
                extension.loadStatus = LoadStatus.NO_ENTRYPOINT
                return
            }

            // Handle defaults
            // If we reach this code, then the extension will most likely be loaded:
            if (extension.version == "Unspecified") {
                LOGGER.warn("Plugin '{}' did not specify a version.", extension.name)
                LOGGER.warn("Plugin '{}' will continue to load but should specify a decouple version.", extension.name)
            }
        }
    }
}