package com.github.zimablue.devoutserver.internal.core.script.nashorn.impl

import com.github.zimablue.devoutserver.internal.core.script.nashorn.NashornHooker
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import org.openjdk.nashorn.api.scripting.ScriptObjectMirror
import javax.script.Invocable
import javax.script.ScriptEngine

/**
 * openjdk nashorn挂钩
 *
 * @constructor 启用openjdk nashorn挂钩
 */
class NashornHookerImpl : NashornHooker() {
    override fun getNashornEngine(args: Array<String>, classLoader: ClassLoader): ScriptEngine {
        return NashornScriptEngineFactory().getScriptEngine(args, classLoader)
    }

    override fun invoke(
        compiledScript: com.github.zimablue.devoutserver.internal.core.script.CompiledScript,
        function: String,
        map: Map<String, Any>?,
        vararg args: Any
    ): Any? {
        val newObject: ScriptObjectMirror =
            (compiledScript.scriptEngine as Invocable).invokeFunction("newObject") as ScriptObjectMirror
        map?.forEach { (key, value) -> newObject[key] = value }
        return newObject.callMember(function, *args)
    }

    override fun isFunction(engine: ScriptEngine, func: Any?): Boolean {
        return func is ScriptObjectMirror && func.isFunction
    }
}