package com.github.zimablue.devoutserver.api.lifecycle

enum class LifeCycle {
    /**
     * 未启动
     */
    NONE,

    /**
     * 插件初始化（静态代码块被执行时）时
     **/
    CONST,

    /**
     * 插件主类被实例化时
     **/
    INIT,

    /**
     * 插件加载时
     **/
    LOAD,

    /**
     * 插件启用时
     **/
    ENABLE,

    /**
     * 服务器完全启动（调度器启动）时
     **/
    ACTIVE,

    /**
     * 插件卸载时
     **/
    DISABLE
}