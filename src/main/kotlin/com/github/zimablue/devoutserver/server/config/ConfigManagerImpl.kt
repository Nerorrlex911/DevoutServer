package com.github.zimablue.devoutserver.server.config


import com.github.zimablue.devoutserver.util.ResourceUtils
import org.slf4j.LoggerFactory
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

object ConfigManagerImpl{
    val LOGGER = LoggerFactory.getLogger("ConfigManager")

    val serverConfig by lazy { Configuration.deserialize<ServerConfig>(Configuration.loadFromFile(File("server.yml"), Type.YAML)) }

    init {

        LOGGER.info("Initializing ConfigManagerImpl,server.yml path: {}",File("server.yml").absolutePath)
        ResourceUtils.extractResource("server.yml", "server.yml", false)

    }




}