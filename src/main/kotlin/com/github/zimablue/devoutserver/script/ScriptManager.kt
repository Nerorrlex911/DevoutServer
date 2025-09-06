package com.github.zimablue.devoutserver.script


import com.github.zimablue.devoutserver.DevoutServer
import com.github.zimablue.devoutserver.script.nashorn.impl.NashornHookerImpl
import com.github.zimablue.devoutserver.util.getAllFiles
import org.slf4j.Logger
import java.io.File
import java.io.Reader
import java.util.concurrent.ConcurrentHashMap
import javax.script.Invocable
import javax.script.ScriptEngine

open class ScriptManager(
    val scriptFolder: File,
    val logger: Logger=DevoutServer.LOGGER,
    val classLoader: ClassLoader=this::class.java.classLoader,
    val loadLib: ScriptEngine.() -> Unit={}
) {
    /**
     * 获取公用ScriptEngine
     */
    val scriptEngine = createNashornEngine()

    /**
     * 获取所有已编译的js脚本文件及路径
     */
    val compiledScripts = ConcurrentHashMap<String, CompiledScript>()

    /**
     * 加载全部脚本
     */
    protected fun loadScripts() {
        for (file in getAllFiles(scriptFolder)) {
            // 以文件相对主目录的路径作为key
            val fileName = file.path
            try {
                compiledScripts[fileName] = CompiledScript(file)
            } catch (error: Throwable) {
                logger.error("Error in loading script $fileName")
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

    /**
     * 执行指定脚本中的指定函数
     * @param name 脚本名(相对于当前工作目录的路径)
     * @param function 函数名
     */
    fun run(name: String,function: String): Any? {
        return run(name,function, null)
    }

    /**
     * 执行指定脚本中的指定函数
     * @param name 脚本名(相对于当前工作目录的路径)
     * @param function 函数名
     * @param map 传入的对象,使用this.<key>访问
     * @param args 传入对应方法的参数
     */
    fun run(name: String,function: String,map: Map<String,Any>?,vararg args: Any) : Any?{
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

    /**
     * 执行指定脚本中的指定函数
     * @param funcPath 函数路径(形似<脚本路径>::<函数名>，相对于scriptFolder的路径)
     * @param map 传入的对象,使用this.<key>访问
     * @param args 传入对应方法的参数
     */
    fun run(funcPath: String,map: Map<String,Any>?,vararg args: Any): Any? {
        val funcArgs = funcPath.split("::")
        val script = funcArgs.getOrNull(0)
            ?: error("Invalid function path: $funcPath, should be <script>.js::<functionName>")
        val function = funcArgs.getOrNull(1)?: "main"
        val relativizedName = scriptFolder.path+File.separator+script
        return run(relativizedName,function,map,*args)
    }

    fun createNashornEngine() = nashornHooker.getNashornEngine(arrayOf("-Dnashorn.args=--language=es6"),classLoader)


    /**
     * 对已编译脚本的简单包装
     */
    inner class CompiledScript {
        var name: String = "unnamed"
            private set
        private val handle: javax.script.CompiledScript

        /**
         * 获取该脚本对应的ScriptEngine
         */
        val scriptEngine: ScriptEngine

        /**
         * 编译js脚本并进行包装, 便于调用其中的指定函数
         *
         * @property reader js脚本文件
         * @property name js脚本文件名, 用于debug标识
         * @property loadLib 在compile之前加载变量或脚本库
         * @constructor 编译js脚本并进行包装
         */
        constructor(reader: Reader, name: String? = null) {
            if (name != null) this.name = name
            scriptEngine = createNashornEngine()
            loadLib.invoke(scriptEngine)
            handle = nashornHooker.compile(scriptEngine, reader)
            magicFunction()
        }

        /**
         * 编译js脚本并进行包装, 便于调用其中的指定函数
         *
         * @property file js脚本文件
         * @property name js脚本文件名, 用于debug标识
         * @property loadLib 在compile之前加载变量或脚本库
         * @constructor 编译js脚本并进行包装
         */
        constructor(file: File,name: String? = null) {
            this.name = name ?: file.path
            scriptEngine = createNashornEngine()
            loadLib.invoke(scriptEngine)
            file.reader().use {
                handle = nashornHooker.compile(scriptEngine, it)
            }
            magicFunction()
        }

        /**
         * 编译js脚本并进行包装, 便于调用其中的指定函数
         *
         * @property script js脚本文本
         * @property name js脚本文件名, 用于debug标识
         * @property loadLib 在compile之前加载变量或脚本库
         * @constructor 编译js脚本并进行包装
         */
        constructor(script: String,name: String? = null) {
            if (name != null) this.name = name
            scriptEngine = createNashornEngine()
            loadLib.invoke(scriptEngine)
            handle = nashornHooker.compile(scriptEngine, script)
            magicFunction()
        }

        /**
         * 执行脚本中的指定函数
         *
         * @param function 函数名
         * @param map 传入的默认对象
         * @param args 传入对应方法的参数
         * @return 解析值
         */
        fun invoke(function: String, map: Map<String, Any>?, vararg args: Any): Any? {
            return nashornHooker.invoke(this, function, map, *args)
        }

        /**
         * 执行脚本中的指定函数
         *
         * @param function 函数名
         * @param args 传入对应方法的参数
         * @return 解析值
         */
        fun simpleInvoke(function: String, vararg args: Any?): Any? {
            return (scriptEngine as Invocable).invokeFunction(function, *args)
        }

        /**
         * 此段代码用于解决js脚本的高并发调用问题, 只可意会不可言传
         */
        private fun magicFunction() {
            handle.eval()
            scriptEngine.eval(
                """
            function NeigeItemsNumberOne() {}
            NeigeItemsNumberOne.prototype = this
            function newObject() { return new NeigeItemsNumberOne() }
        """
            )
        }

        fun run(function: String) {
            try {
                invoke(function,null)
            } catch (error: Throwable) {
                logger.error("Error in $function of ${this.name}")
                error.printStackTrace()
            }
        }
        /**
         * 判断脚本中是否存在指定函数
         * @param func 函数名
         */
        fun isFunction(func: String) = nashornHooker.isFunction(scriptEngine, func)
    }

    companion object {
        val nashornHooker by lazy { NashornHookerImpl() }
    }
}