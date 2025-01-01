package com.github.zimablue.devoutserver.feature.luckperms

import me.lucko.luckperms.common.config.generic.adapter.ConfigurationAdapter
import me.lucko.luckperms.common.plugin.LuckPermsPlugin
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File


class YamlConfigurationAdapter(val plugin: LuckPermsPlugin,val file: File) : ConfigurationAdapter {
    var configuration = Configuration.loadFromFile(file, Type.YAML)

    override fun getPlugin(): LuckPermsPlugin {
        return plugin
    }

    override fun reload() {
        configuration = Configuration.loadFromFile(file, Type.YAML)
    }

    override fun getString(path: String, def: String?): String? {
        return configuration.getString(path, def)
    }

    override fun getInteger(path: String, def: Int): Int {
        return configuration.getInt(path, def)
    }

    override fun getBoolean(path: String, def: Boolean): Boolean {
        return configuration.getBoolean(path, def)
    }

    override fun getStringList(path: String, def: List<String>?): List<String>? {
        val list = configuration.getStringList(path)
        return if (configuration.isList(path)) list else def
    }

    override fun getStringMap(path: String, def: Map<String, String?>): Map<String, String?> {
        val map: MutableMap<String, String?> = HashMap()
        val section = configuration.getConfigurationSection(path) ?: return def

        for (key in section.getKeys(false)) {
            map[key] = section.getString(key)
        }

        return map
    }



}