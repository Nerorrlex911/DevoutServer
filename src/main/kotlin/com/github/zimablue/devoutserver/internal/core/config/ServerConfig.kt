package com.github.zimablue.devoutserver.internal.core.config

data class ServerConfig(
    val debug: Boolean,
    val address: String,
    val port: Int,
) {
}