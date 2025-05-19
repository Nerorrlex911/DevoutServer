package com.github.zimablue.devoutserver.feature.luckperms

import com.github.zimablue.devoutserver.lifecycle.Awake
import com.github.zimablue.devoutserver.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.lifecycle.LifeCycle
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
    @Awake(LifeCycle.LOAD, AwakePriority.NORMAL)
    fun init() {
        val directory: Path = Path.of("luckperms")
        extractResource("luckperms.yml")
        com.github.zimablue.devoutserver.feature.luckperms.LuckPerms.luckPerms = LuckPermsMinestom.builder(directory)
            .commandRegistry(CommandRegistry.minestom()) // enables registration of LuckPerms commands
            //.contextProvider(DummyContextProvider()) // provide additional custom contexts
            .configurationAdapter { plugin ->
                MultiConfigurationAdapter(
                    plugin,  // define the configuration
                    EnvironmentVariableConfigAdapter(plugin),  // use MultiConfigurationAdapter to load from multiple sources, in order
                    com.github.zimablue.devoutserver.feature.luckperms.YamlConfigurationAdapter(
                        plugin,
                        File("luckperms.yml")
                    ) // load from a yaml file
                )
            }
            .dependencyManager(false) // automatically download and classload dependencies
            .enable()
    }
}