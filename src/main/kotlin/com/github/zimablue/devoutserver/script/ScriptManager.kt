package com.github.zimablue.devoutserver.script


import com.github.zimablue.devoutserver.DevoutServer
import com.github.zimablue.devoutserver.DevoutServer.currentDir
import com.github.zimablue.devoutserver.script.CompiledScript.Companion.nashornHooker

import com.github.zimablue.devoutserver.util.getAllFiles
import org.slf4j.Logger
import java.io.File

open class ScriptManager(val scriptFolder: File,val logger: Logger=DevoutServer.LOGGER) {
    /**
     * 获取公用ScriptEngine
     */
    val scriptEngine = nashornHooker.getNashornEngine()

    /**
     * 获取所有已编译的js脚本文件及路径
     */
    val compiledScripts = HashMap<String, CompiledScript>()


    /**
     * 加载全部脚本
     */
    protected fun loadScripts() {
        for (file in getAllFiles(scriptFolder)) {
            // 以文件相对主目录的路径作为key
            val fileName = currentDir.relativize(file.absoluteFile.toPath()).toString()
            try {
                compiledScripts[fileName] = CompiledScript(file)
            } catch (error: Throwable) {
                //todo error message
                error.printStackTrace()
            }
        }
    }

    /**
     * 重载脚本管理器
     */
    fun reload() {
        compiledScripts.clear()
        loadScripts()
    }

    fun run(name: String,function: String): Any? {
        return run(name,function, null)
    }

    fun run(name: String,function: String,map: Map<String,Any>?,vararg args: Any) : Any?{
        //val relativizedName = currentDir.relativize(scriptFolder.absoluteFile.toPath().resolve(name)).toString()
        val script = compiledScripts[name] ?: return null
        if (nashornHooker.isFunction(script.scriptEngine, function)) {
            val result = try {
                script.invoke(function, map, *args)
            } catch (error: Throwable) {
                logger.error("Error in $function of ${script.name}")
                error.printStackTrace()
                null
            }
            return result
        }
        return null
    }
}