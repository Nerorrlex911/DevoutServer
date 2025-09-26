package com.github.zimablue.devoutserver.lang

import com.github.zimablue.devoutserver.server.lifecycle.Awake
import com.github.zimablue.devoutserver.server.lifecycle.LifeCycle
import java.io.File

object LangManagerImpl : LangManager() {
    @Awake(LifeCycle.LOAD)
    fun onLoad() {
        init(File("lang"))
    }
}