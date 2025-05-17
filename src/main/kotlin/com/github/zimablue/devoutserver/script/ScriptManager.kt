package com.github.zimablue.devoutserver.script

import com.github.zimablue.devoutserver.DevoutServer
import com.github.zimablue.devoutserver.DevoutServer.nashornHooker
import com.github.zimablue.devoutserver.script.ScriptManagerImpl.Logger
import com.github.zimablue.devoutserver.util.getAllFiles
import java.io.File

open class ScriptManager(val path: File) {
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
        for (file in getAllFiles(path)) {
            val fileName = file.path
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
        val absoluteName = path.absolutePath + File.separator + name
        val script = compiledScripts[absoluteName] ?: return null
        if (nashornHooker.isFunction(script.scriptEngine, function)) {
            val result = try {
                script.invoke(function, map, *args)
            } catch (error: Throwable) {
                Logger.error("Error in $function of ${script.name}")
                error.printStackTrace()
                null
            }
            return result
        }
        return null
    }
}