package com.github.zimablue.devoutserver

import com.github.zimablue.devoutserver.server.ServerDependencyManager
import com.github.zimablue.devoutserver.server.terminal.EasyTerminal


fun main(args: Array<String>) {
    EasyTerminal.start()
    ServerDependencyManager.loadDependencies()
    DevoutServer.start()
    //MinecraftServer.getBiomeManager().loadVanillaBiomes()
}