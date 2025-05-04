package com.github.zimablue.devoutserver.script

import com.github.zimablue.devoutserver.DevoutServer.nashornHooker
import com.github.zimablue.devoutserver.lifecycle.Awake
import com.github.zimablue.devoutserver.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.lifecycle.LifeCycle
import org.slf4j.LoggerFactory
import java.io.File

object ScriptManagerImpl: ScriptManager(File("scripts")) {

    val Logger = LoggerFactory.getLogger(ScriptManagerImpl::class.java)

    @Awake(LifeCycle.ENABLE,AwakePriority.NORMAL)
    fun onEnable() {
        compiledScripts.forEach { (name, _) ->
            Logger.info("Enabling script $name")
            run(name,"onEnable")
        }
    }
    @Awake(LifeCycle.DISABLE,AwakePriority.NORMAL)
    fun onDisable() {
        compiledScripts.forEach { (name, _) ->
            Logger.info("Disabling script $name")
            run(name,"onDisable")
        }
    }

    fun run(name: String,function: String) {
        val script = compiledScripts[name] ?: return
        if (nashornHooker.isFunction(script.scriptEngine, function)) {
            try {
                script.invoke(function, null)
            } catch (error: Throwable) {
                Logger.error("Error in $function of ${script.name}")
                error.printStackTrace()
            }
        }
    }

}