package com.github.zimablue.devoutserver.config

data class ServerConfig(
    val debug: Boolean,
    val address: String,
    val port: Int,
) {
}