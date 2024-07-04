package com.github.zimablue.devoutserver.internal.manager

import DevoutServer
import taboolib.module.configuration.Configuration

object ConfigManagerImpl : Manager{
    lateinit var config: Configuration
    var debug = config.getBoolean("debug",false)
    override val priority: Int = 1

    override val key = "ConfigManager"

}