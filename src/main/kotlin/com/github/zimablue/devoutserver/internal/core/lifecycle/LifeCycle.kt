package com.github.zimablue.devoutserver.internal.core.lifecycle

enum class LifeCycle {
    NONE,
    //jar文件加载为Plugin后执行
    LOAD,
    //Plugin启用
    ENABLE,
    //首个世界开始加载时
    ACTIVE,
    //Plugin停用
    DISABLE,
}