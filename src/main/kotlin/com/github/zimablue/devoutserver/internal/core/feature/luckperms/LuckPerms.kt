package com.github.zimablue.devoutserver.internal.core.feature.luckperms

import com.github.zimablue.devoutserver.internal.core.lifecycle.Awake
import com.github.zimablue.devoutserver.internal.core.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.internal.core.lifecycle.LifeCycle
import com.github.zimablue.devoutserver.util.ResourceUtils.extractResource
import me.lucko.luckperms.common.config.generic.adapter.EnvironmentVariableConfigAdapter
import me.lucko.luckperms.common.config.generic.adapter.MultiConfigurationAdapter
import me.lucko.luckperms.minestom.CommandRegistry
import me.lucko.luckperms.minestom.LuckPermsMinestom
import net.luckperms.api.LuckPerms
import java.io.File
import java.nio.file.Path

object LuckPerms {
    lateinit var luckPerms: LuckPerms
    @Awake(LifeCycle.LOAD, 0)
    fun init() {
        val directory: Path = Path.of("luckperms")
        extractResource("luckperms.yml")
        luckPerms = LuckPermsMinestom.builder(directory)
            .commandRegistry(CommandRegistry.minestom()) // enables registration of LuckPerms commands
            //.contextProvider(DummyContextProvider()) // provide additional custom contexts
            .configurationAdapter { plugin ->
                MultiConfigurationAdapter(
                    plugin,  // define the configuration
                    EnvironmentVariableConfigAdapter(plugin),  // use MultiConfigurationAdapter to load from multiple sources, in order
                    YamlConfigurationAdapter(plugin, File("luckperms.yml")) // load from a yaml file
                )
            }
            .dependencyManager(true) // automatically download and classload dependencies
            .enable()
    }
}