package com.github.zimablue.devoutserver.lang

import com.github.zimablue.devoutserver.lang.impl.TextSection
import com.github.zimablue.devoutserver.util.map.BaseMap
import taboolib.module.configuration.Configuration
import java.io.File

class LangFile(val file: File) : BaseMap<String, List<LangSection>>() {
    init {
        load()
    }

    fun load() {
        clear()
        val config = Configuration.loadFromFile(file)
        config.getKeys(false).forEach { key ->

            if(config.isString(key)) {
                put(key, listOf(TextSection(config.getString(key)!!)))
                return@forEach
            }
            // load as a list of sections
            val value = config.getList(key)?: emptyList<Any>()
            val sections = value.map { map ->
                // If the value is a string, convert it to a single TextSection
                if(map is String) {
                    return@map TextSection(map)
                }
                // Otherwise, it should be a map
                val type = (map as Map<String,Any>)["type"].toString()
                val section = LangSection.create(type, map) ?: error("Unknown lang section type: $type")
                return@map section
            }
            put(key, sections)
        }
    }
}