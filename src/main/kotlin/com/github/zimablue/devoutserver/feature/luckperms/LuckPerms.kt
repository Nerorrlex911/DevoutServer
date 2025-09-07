package com.github.zimablue.devoutserver.feature.luckperms

import com.github.zimablue.devoutserver.feature.luckperms.LuckPerms.hasPermission
import com.github.zimablue.devoutserver.server.lifecycle.Awake
import com.github.zimablue.devoutserver.server.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.server.lifecycle.LifeCycle
import com.github.zimablue.devoutserver.util.ResourceUtils.extractResource
import me.lucko.luckperms.common.config.generic.adapter.EnvironmentVariableConfigAdapter
import me.lucko.luckperms.common.config.generic.adapter.MultiConfigurationAdapter
import me.lucko.luckperms.minestom.CommandRegistry
import me.lucko.luckperms.minestom.LuckPermsMinestom
import net.luckperms.api.LuckPerms
import net.luckperms.api.util.Tristate
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

object LuckPerms {
    lateinit var luckPerms: LuckPerms
    val adapter by lazy { luckPerms.getPlayerAdapter(Player::class.java) }
    @Awake(LifeCycle.LOAD, AwakePriority.NORMAL)
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
                    YamlConfigurationAdapter(
                        plugin,
                        File("luckperms.yml")
                    ) // load from a yaml file
                )
            }
            .logger(LoggerFactory.getLogger("LuckPerms"))
            .enable()
    }

    fun Player.hasPermission(permission: String) : Boolean {
        return getPermission(this,permission).asBoolean()
    }

    fun getPermission(player: Player,permission: String) : Tristate =
        adapter.getUser(player).cachedData.permissionData.checkPermission(permission)

    fun CommandSender.hasPermission(permission: String): Boolean {
        return if (this is Player) {
            this.hasPermission(permission)
        } else {
            true
        }
    }
}