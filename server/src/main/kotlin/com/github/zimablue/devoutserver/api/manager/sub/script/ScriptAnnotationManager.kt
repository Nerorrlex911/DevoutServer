package com.github.zimablue.devoutserver.api.manager.sub.script

import com.github.zimablue.devoutserver.api.manager.Manager
import com.github.zimablue.devoutserver.api.decouple.map.KeyMap
import com.github.zimablue.devoutserver.api.script.annotation.ScriptAnnotation

/** 脚本注解管理器 主要负责维护脚本注解 */
abstract class ScriptAnnotationManager : Manager, KeyMap<String, ScriptAnnotation>()