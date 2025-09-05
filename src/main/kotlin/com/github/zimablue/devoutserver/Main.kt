package com.github.zimablue.devoutserver

import com.github.zimablue.devoutserver.server.ServerDependencyManager
import com.github.zimablue.devoutserver.server.terminal.EasyTerminal


fun main(args: Array<String>) {
    ServerDependencyManager.loadDependencies()
    DevoutServer.start()
    EasyTerminal.start()
    //MinecraftServer.getBiomeManager().loadVanillaBiomes()
}