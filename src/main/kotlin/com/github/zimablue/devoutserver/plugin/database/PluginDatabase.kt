package com.github.zimablue.devoutserver.plugin.database

import com.github.zimablue.devoutserver.plugin.Plugin
import com.github.zimablue.devoutserver.plugin.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.plugin.lifecycle.PluginLifeCycle
import com.github.zimablue.devoutserver.util.ResourceUtils
import com.zaxxer.hikari.HikariDataSource
import taboolib.module.configuration.Configuration
import taboolib.module.database.Database
import taboolib.module.database.Database.createDataSourceWithoutConfig
import taboolib.module.database.Host
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource
import kotlin.io.path.absolute

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
     * 在插件配置文件夹下释放datasource.yml
     * 并注册关闭数据库连接的周期任务
     */
    fun setupDatabase(plugin: Plugin) {
        // datasource.yml
        val settingsPath = File("${plugin.dataDirectory.absolute()}${File.separator}datasource.yml")
        ResourceUtils.extractResource(
            "datasource.yml",
            settingsPath.toString(),
            false
        )
        val settings = Configuration.loadFromFile(
            File("${plugin.dataDirectory.absolute()}${File.separator}datasource.yml")
        )
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