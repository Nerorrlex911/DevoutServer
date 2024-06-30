package com.github.zimablue.devoutserver.api.plugin

import com.google.gson.JsonObject
import net.minestom.server.utils.validate.Check
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.nio.file.Path
import java.util.*

/**
 * Represents an extension from an `extension.json` that is capable of powering an Plugin object.
 *
 * This has no constructor as its properties are set via GSON.
 */
data class DiscoveredPlugin(
    /** Name of the DiscoveredPlugin. Unique for all extensions.  */
    var name: String = "",

    /** Main class of this DiscoveredPlugin, must extend Plugin.  */
    var entrypoint: String = "",

    /** Version of this extension, highly reccomended to set it.  */
    private var version: String = "Unspecified",

    /** People who have made this extension.  */
    var authors: List<String> = listOf(),

    /** List of extension names that this depends on.  */
    var dependencies: MutableList<String> = mutableListOf(),

    /** List of Repositories and URLs that this depends on.  */
    var externalDependencies: ExternalDependencies = ExternalDependencies(),
    /**
     * Extra meta on the object.
     * Do NOT use as configuration:
     *
     * Meta is meant to handle properties that will
     * be accessed by other extensions, not accessed by itself
     */
    var meta: Map<*,*> = mapOf<String,String>()
) {

    /** All files of this extension  */
    @Transient
    var files = LinkedList<URL>()

    /** The load status of this extension -- LOAD_SUCCESS is the only good one.  */
    @Transient
    var loadStatus: LoadStatus = LoadStatus.LOAD_SUCCESS

    /** The original jar this is from.  */
    @Transient
    var originalJar: File? = null

    @Transient
    var dataDirectory: Path? = null


    /** The class loader that powers it.  */
    @Transient
    var classLoader: PluginClassLoader? = null

    fun getExternalDependencies(): ExternalDependencies {
        return externalDependencies!!
    }

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

    data class ExternalDependencies(
        var repositories: ArrayList<Repository> = arrayListOf(),
        var artifacts: ArrayList<String> = arrayListOf()
    ) {
        class Repository(
            val name: String,
            val url: String,
        )
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