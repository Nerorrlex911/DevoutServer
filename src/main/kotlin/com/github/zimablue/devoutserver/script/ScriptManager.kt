package com.github.zimablue.devoutserver.script

import DevoutServer.nashornHooker
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

    init {
        // 加载全部脚本
        loadScripts()
    }

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
}