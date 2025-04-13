package com.github.zimablue.devoutserver.config

import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

object ConfigManagerImpl{
    val serverConfig = Configuration.deserialize<ServerConfig>(Configuration.loadFromFile(File("config.yml"),Type.YAML))

    fun init() {
    }




}