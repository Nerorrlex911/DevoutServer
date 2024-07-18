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
class DiscoveredPlugin {
    /** Name of the DiscoveredPlugin. Unique for all extensions.  */
    var name: String? = null

    /** Main class of this DiscoveredPlugin, must extend Plugin.  */
    var entrypoint: String? = null

    /** Version of this extension, highly reccomended to set it.  */
    private var version: String? = null

    /** People who have made this extension.  */
    var authors = listOf<String>()
        private set

    /** List of extension names that this depends on.  */
    var dependencies = mutableListOf<String>()
        private set

    /** List of extension names that this soft-depends on. */
    var softDependencies = mutableListOf<String>()
        private set

    /** List of extension names that load before this*/
    var loadBefore = mutableListOf<String>()

    /** List of Repositories and URLs that this depends on.  */
    var externalDependencies: ExternalDependencies? = null

    /**
     * Extra meta on the object.
     * Do NOT use as configuration:
     *
     * Meta is meant to handle properties that will
     * be accessed by other extensions, not accessed by itself
     */
    private var meta: JsonObject? = null

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
    var classLoader: PluginClassLoader? = null


    fun getEntrypoint(): String {
        return entrypoint!!
    }

    fun getVersion(): String {
        return version!!
    }

    fun getExternalDependencies(): ExternalDependencies {
        return externalDependencies!!
    }

    fun createClassLoader() {
        Check.stateCondition(classLoader != null, "Plugin classloader has already been created")
        val urls = files.toTypedArray<URL>()
        classLoader = PluginClassLoader(this.name!!, urls,  discoveredPlugin = this)
    }


    fun getMeta(): JsonObject {
        return meta!!
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

    class ExternalDependencies {
        var repositories: Array<Repository> = arrayOf()
        var artifacts: Array<String> = arrayOf()

        class Repository {
            val name: String?=null
            val url: String?=null
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
            if (extension.name == null) {
                val fileList = StringBuilder()
                for (f in extension.files) {
                    fileList.append(f.toExternalForm()).append(", ")
                }
                LOGGER.error("Plugin with no name. (at {}})", fileList)
                LOGGER.error("Plugin at ({}) will not be loaded.", fileList)
                extension.loadStatus = LoadStatus.INVALID_NAME

                // To ensure @NotNull: name = INVALID_NAME
                extension.name = extension.loadStatus.name
                return
            }

            if (!extension.name!!.matches(NAME_REGEX.toRegex())) {
                LOGGER.error("Plugin '{}' specified an invalid name.", extension.name)
                LOGGER.error("Plugin '{}' will not be loaded.", extension.name)
                extension.loadStatus = LoadStatus.INVALID_NAME

                // To ensure @NotNull: name = INVALID_NAME
                extension.name = extension.loadStatus.name
                return
            }

            if (extension.entrypoint == null) {
                LOGGER.error("Plugin '{}' did not specify an entry point (via 'entrypoint').", extension.name)
                LOGGER.error("Plugin '{}' will not be loaded.", extension.name)
                extension.loadStatus = LoadStatus.NO_ENTRYPOINT

                // To ensure @NotNull: entrypoint = NO_ENTRYPOINT
                extension.entrypoint = extension.loadStatus.name
                return
            }

            // Handle defaults
            // If we reach this code, then the extension will most likely be loaded:
            if (extension.version == null) {
                LOGGER.warn("Plugin '{}' did not specify a version.", extension.name)
                LOGGER.warn("Plugin '{}' will continue to load but should specify a decouple version.", extension.name)
                extension.version = "Unspecified"
            }

            if (extension.softDependencies == null) {
                extension.softDependencies = ArrayList()
            }

            if (extension.loadBefore == null) {
                extension.loadBefore = ArrayList()
            }

            // No external dependencies were specified;
            if (extension.externalDependencies == null) {
                extension.externalDependencies = ExternalDependencies()
            }

            // No meta was provided
            if (extension.meta == null) {
                extension.meta = JsonObject()
            }
        }
    }
}