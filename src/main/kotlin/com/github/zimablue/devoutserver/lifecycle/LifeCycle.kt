package com.github.zimablue.devoutserver.lifecycle

enum class LifeCycle {
    //生命周期管理器的初始状态，一经加载就会立即执行
    NONE,
    //jar文件加载为Plugin后执行
    LOAD,
    //Plugin启用
    ENABLE,
    //首个世界开始加载时
    ACTIVE,
    //Plugin停用
    DISABLE,
    //服务器关闭
    SHUTDOWN
}