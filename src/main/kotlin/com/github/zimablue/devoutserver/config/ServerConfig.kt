package com.github.zimablue.devoutserver.config

import com.electronwill.nightconfig.core.conversion.Path

data class ServerConfig(
    val debug: Boolean=false,
    val address: String="127.0.0.1",
    val port: Int=25565,
    @Path("plugin-folder")
    val pluginFolder: String="plugins",
) {
}