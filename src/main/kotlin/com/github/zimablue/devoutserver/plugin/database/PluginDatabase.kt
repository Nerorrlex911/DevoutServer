package com.github.zimablue.devoutserver.plugin.database

import com.github.zimablue.devoutserver.plugin.Plugin
import com.github.zimablue.devoutserver.plugin.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.plugin.lifecycle.PluginLifeCycle
import com.zaxxer.hikari.HikariDataSource
import taboolib.module.configuration.Configuration
import taboolib.module.database.Database
import taboolib.module.database.Database.createDataSourceWithoutConfig
import taboolib.module.database.Host
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

object PluginDatabase {
    val settingsFiles = ConcurrentHashMap<Plugin, Configuration>()

    val datasources = ConcurrentHashMap<Plugin, HikariDataSource>()

    val callbackClose = ConcurrentHashMap<Plugin, Runnable>()

    fun release(plugin: Plugin) {
        datasources[plugin]?.close()
        callbackClose[plugin]?.run()
    }
    /**
     * 给插件部署数据库
     * 并注册关闭数据库连接的周期任务
     * @param settings 指定配置文件，默认使用插件的 datasource.yml 文件，如果不存在则使用主程序目录下的 datasource.yml 文件
     */
    fun setupDatabase(plugin: Plugin,settings: Configuration=Database.settingsFile) {
        // datasource.yml
        settingsFiles[plugin]= settings
        // lifeCycle
        plugin.lifeCycleManager.registerTask(PluginLifeCycle.DISABLE, AwakePriority.HIGH) {
            release(plugin)
        }
    }

    /**
     * 对指定插件创建一个数据库连接池
     * @param autoRelease 在插件卸载时自动释放数据库连接
     */
    fun createDataSource(plugin: Plugin, host: Host<*>, autoRelease: Boolean = true, withoutConfig: Boolean = false): DataSource {
        val pluginSettingsFile = settingsFiles[plugin]?:error("datasource.yml not found: $plugin")
        val dataSource = if (withoutConfig) createDataSourceWithoutConfig(host) else Database.createDataSource(
            host,
            settingsFile = pluginSettingsFile
        )
        return dataSource.also {
            if (autoRelease) {
                datasources[plugin] = it as HikariDataSource
            }
        }
    }

    /**
     * 创建一个关闭数据库连接的回调函数
     */
    fun prepareClose(plugin: Plugin, func: Runnable) {
        callbackClose[plugin] = func
    }
}