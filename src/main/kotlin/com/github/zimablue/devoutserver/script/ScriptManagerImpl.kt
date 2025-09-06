package com.github.zimablue.devoutserver.script

import com.github.zimablue.devoutserver.server.lifecycle.Awake
import com.github.zimablue.devoutserver.server.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.server.lifecycle.LifeCycle
import com.github.zimablue.devoutserver.util.ResourceUtils
import org.slf4j.LoggerFactory
import java.io.File

object ScriptManagerImpl: ScriptManager(File("scripts").absoluteFile,LoggerFactory.getLogger("ScriptManagerImpl")) {


    init {
        ResourceUtils.extractResource("scripts")
    }

    @Awake(LifeCycle.LOAD, AwakePriority.NORMAL)
    fun onLoad() {
        loadScripts()
    }

    @Awake(LifeCycle.ENABLE, AwakePriority.NORMAL)
    fun onEnable() {
        compiledScripts.forEach { (name, _) ->
            logger.info("Enabling script $name")
            run(name,"onEnable")
        }
    }
    @Awake(LifeCycle.DISABLE, AwakePriority.NORMAL)
    fun onDisable() {
        compiledScripts.forEach { (name, _) ->
            logger.info("Disabling script $name")
            run(name,"onDisable")
        }
    }


}