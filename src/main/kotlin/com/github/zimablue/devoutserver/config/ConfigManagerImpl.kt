package com.github.zimablue.devoutserver.config

import com.github.zimablue.devoutserver.internal.manager.Manager
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import java.io.File

object ConfigManagerImpl : Manager {
    val serverConfig = Configuration.deserialize<com.github.zimablue.devoutserver.config.ServerConfig>(Configuration.loadFromFile(File("config.yml"),Type.YAML))
    override val priority: Int = 1
    override val key = "ConfigManager"
    /**
     * 初始化
     * 在init中的内容会按照优先级依次执行

     */
    override fun init() {
    }




}