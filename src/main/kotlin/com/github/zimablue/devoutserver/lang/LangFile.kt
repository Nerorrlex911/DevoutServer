package com.github.zimablue.devoutserver.lang

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
            val value = config.getMapList(key)
            val sections = value.map { map ->
                val type = map["type"] as String
                val section = LangSection.create(type,map as Map<String,Any>) ?: error("Unknown lang section type: $type")
                section
            }
            put(key, sections)
        }
    }
}