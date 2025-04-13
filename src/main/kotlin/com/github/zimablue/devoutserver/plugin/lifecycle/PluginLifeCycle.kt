package com.github.zimablue.devoutserver.plugin.lifecycle

enum class PluginLifeCycle: Comparable<PluginLifeCycle> {
    NONE,
    //jar文件加载为Plugin后执行
    LOAD,
    //Plugin启用
    ENABLE,
    //首个世界开始加载时
    ACTIVE,
    //Plugin停用
    DISABLE;

}