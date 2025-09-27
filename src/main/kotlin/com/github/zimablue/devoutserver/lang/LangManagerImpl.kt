package com.github.zimablue.devoutserver.lang

import com.github.zimablue.devoutserver.server.lifecycle.Awake
import com.github.zimablue.devoutserver.server.lifecycle.LifeCycle
import com.github.zimablue.devoutserver.util.ResourceUtils
import net.minestom.server.MinecraftServer
import java.io.File

object LangManagerImpl : LangManager() {
    @Awake(LifeCycle.LOAD)
    fun onLoad() {
        ResourceUtils.extractResource("lang")
        reload(File("lang"))
    }
}