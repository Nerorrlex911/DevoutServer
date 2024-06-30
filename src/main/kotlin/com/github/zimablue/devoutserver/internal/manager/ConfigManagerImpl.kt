package com.github.zimablue.devoutserver.internal.manager

import DevoutServer
import taboolib.module.configuration.Configuration

object ConfigManagerImpl {
    lateinit var config: Configuration
    var debug = config.getBoolean("debug",false)
    fun onLoad() {
        //todo
    }
}