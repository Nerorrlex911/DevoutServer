package com.github.zimablue.devoutserver.plugin.config

import com.github.zimablue.devoutserver.lifecycle.Awake
import com.github.zimablue.devoutserver.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.lifecycle.LifeCycle
import com.github.zimablue.devoutserver.plugin.PluginManager
import com.github.zimablue.devoutserver.plugin.PluginManagerImpl
import com.github.zimablue.devoutserver.plugin.annotation.AnnotationManager
import org.slf4j.LoggerFactory
import org.tabooproject.reflex.FastInstGetter
import taboolib.common5.Coerce
import taboolib.module.configuration.ConfigNodeFile
import taboolib.module.configuration.ConfigNodeTransfer
import taboolib.module.configuration.Configuration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object ConfigLoader {
    val LOGGER = LoggerFactory.getLogger(ConfigLoader::class.java)!!
    val files = HashMap<String, ConfigNodeFile>()
    val configNodes = ConcurrentHashMap<String, CopyOnWriteArrayList<ConfigNodeField>>()
    fun reload() {
        //todo 自动重载
    }
    @Awake(LifeCycle.LOAD,AwakePriority.LOW)
    fun onLoad() {
        for (plugin in PluginManagerImpl.values) {
            //加载配置文件
            val configFields = AnnotationManager.getTargets<Config>(plugin).first
            configFields.forEach { configField ->
                val configAnno = configField.getAnnotation(Config::class.java)
                val name = configAnno.value
                val target = configAnno.target.ifEmpty { name }
                val instance = FastInstGetter(configField.declaringClass.name).instance//todo exception handle
                if(files.containsKey(name)) {
                    configField.set(instance, files[name])
                } else {
                    plugin.savePackagedResource(target)
                    val file = plugin.dataDirectory.resolve(target).toFile()
                    val conf =Configuration.loadFromFile(file)
                    configField.set(instance, conf)
                    val configNodeFile = ConfigNodeFile(conf, file)
                    //todo 节点加载

                    files[name] = configNodeFile
                }
            }
            //加载配置节点
            val configNodeFields = AnnotationManager.getTargets<ConfigNode>(plugin).first
            configNodeFields.forEach { configNodeField ->
                val configNodeAnno = configNodeField.getAnnotation(ConfigNode::class.java)
                val bind = configNodeAnno.bind.ifEmpty { "config.yml" }
                val file = files[bind]
                if(file == null) {
                    LOGGER.warn("$bind not defined: $configNodeField")
                    return@forEach
                }
                val value = configNodeAnno.value.ifEmpty { configNodeField.name }
                var data = file.configuration[value]
                if (data == null) {
                    LOGGER.warn("$value not found in $bind")
                    return
                }
                val instance = FastInstGetter(configNodeField.declaringClass.name).instance//todo exception handle
                if(configNodeField.type==ConfigNodeTransfer::class.java) {
                    val transfer = configNodeField.get(instance) as ConfigNodeTransfer<*, *>
                    transfer.update(data)
                } else {
                    when (configNodeField.type) {
                        Integer::class.java -> data = Coerce.toInteger(data)
                        Character::class.java -> data = Coerce.toChar(data)
                        java.lang.Byte::class.java -> data = Coerce.toByte(data)
                        java.lang.Long::class.java -> data = Coerce.toLong(data)
                        java.lang.Double::class.java -> data = Coerce.toDouble(data)
                        java.lang.Float::class.java -> data = Coerce.toFloat(data)
                        java.lang.Short::class.java -> data = Coerce.toShort(data)
                        java.lang.Boolean::class.java -> data = Coerce.toBoolean(data)
                    }
                    configNodeField.set(instance, data)
                }


            }
        }
    }

}