package taboolib.module.configuration
/*
import org.tabooproject.reflex.ClassField
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.PrimitiveSettings
import taboolib.common.inject.ClassVisitor
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.function.releaseResourceFile
import java.util.function.Supplier

@Awake
class ConfigLoader : ClassVisitor(1) {

    @Suppress("DEPRECATION")
    override fun visit(field: ClassField, clazz: Class<*>, instance: Supplier<*>?) {
        if (field.isAnnotationPresent(Config::class.java)) {
            val configAnno = field.getAnnotation(Config::class.java)
            val name = configAnno.property("value", "config.yml")
            val target = configAnno.property("target", name).let {
                it.ifEmpty { name }
            }
            if (files.containsKey(name)) {
                field.set(instance?.get(), files[name]!!.configuration)
            } else {
                val file = releaseResourceFile(name, target = target)
                // 兼容模式加载
                val conf = if (field.fieldType == SecuredFile::class.java) SecuredFile.loadConfiguration(file) else Configuration.loadFromFile(file)
                // 赋值
                field.set(instance?.get(), conf)
                //TODO 自动重载?
                val configFile = ConfigNodeFile(conf, file)
                conf.onReload {
                    val loader = PlatformFactory.getAPI<ConfigNodeLoader>()
                    configFile.nodes.forEach { loader.visit(it, clazz, instance) }
                }
                files[name] = configFile
                // 开发模式
                if (PrimitiveSettings.IS_DEBUG_MODE) {
                    PrimitiveIO.println("Loaded config file: ${file.absolutePath}")
                }
            }
        }
    }

    override fun getLifeCycle(): LifeCycle {
        return LifeCycle.INIT
    }

    companion object {

        val files = HashMap<String, ConfigNodeFile>()

    }
}
 */