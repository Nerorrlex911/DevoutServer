package com.github.zimablue.devoutserver

import com.github.zimablue.devoutserver.server.ServerDependencyManager


fun main(args: Array<String>) {
    ServerDependencyManager.loadDependencies()
    DevoutServer.start()
    //MinecraftServer.getBiomeManager().loadVanillaBiomes()
}