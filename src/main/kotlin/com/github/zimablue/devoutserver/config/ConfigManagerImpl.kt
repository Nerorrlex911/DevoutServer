package com.github.zimablue.devoutserver.config


import com.github.zimablue.devoutserver.util.ResourceUtils
import org.slf4j.LoggerFactory
import org.tinylog.Logger
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

object ConfigManagerImpl{
    val logger = LoggerFactory.getLogger(ConfigManagerImpl::class.java)

    val serverConfig by lazy { Configuration.deserialize<ServerConfig>(Configuration.loadFromFile(File("server.yml"), Type.YAML)) }

    init {

        Logger.info("Initializing ConfigManagerImpl,server.yml path: {}",File("server.yml").absolutePath)
        ResourceUtils.extractResource("server.yml", "server.yml", false)

    }




}