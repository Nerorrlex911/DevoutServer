package com.github.zimablue.devoutserver.api.manager.sub.script

import com.skillw.pouvoir.Pouvoir
import com.github.zimablue.devoutserver.api.manager.Manager
import com.github.zimablue.devoutserver.api.decouple.map.KeyMap
import com.github.zimablue.devoutserver.api.script.PouFileCompiledScript
import com.github.zimablue.devoutserver.api.script.engine.hook.PouCompiler
import com.skillw.pouvoir.internal.core.script.javascript.PouJavaScriptEngine
import java.io.File
import javax.script.CompiledScript

/**
 * @className CompileManager
 *
 * 脚本编译管理器
 *
 * 主要负责编译脚本文件 脚本字符串
 *
 * @author Glom
 * @date 2022/7/28 2:08 Copyright 2022 user. 
 */
abstract class CompileManager : Manager, KeyMap<String, PouCompiler>() {

    /**
     * 编译脚本文件
     *
     * @param file 你需要编译的文件
     * @return 预编译脚本
     */
    abstract fun compile(file: File): PouFileCompiledScript?

    /**
     * 编译脚本字符串
     *
     * @param script 脚本字符串
     * @return 预编译脚本
     */
    abstract fun compile(script: String, vararg params: String): CompiledScript

    /**
     * 编译脚本字符串
     *
     * @param script 脚本字符串
     * @param engine 指定脚本引擎
     * @return 预编译脚本
     */
    abstract fun compile(
        script: String,
        vararg params: String,
        engine: PouCompiler = PouJavaScriptEngine,
    ): CompiledScript

    companion object {
        /**
         * 将文件编译成预编译脚本
         *
         * @return PouFileCompiledScript? 预编译脚本
         * @receiver File 文件
         */
        @JvmStatic
        fun File.compileScript(): PouFileCompiledScript? {
            return Pouvoir.compileManager.compile(this)
        }
    }

}