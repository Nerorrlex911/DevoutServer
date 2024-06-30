package com.github.zimablue.devoutserver.api.manager

import com.github.zimablue.devoutserver.api.decouple.SubPouvoir
import com.github.zimablue.devoutserver.api.decouple.TotalManager
import com.github.zimablue.devoutserver.api.decouple.map.KeyMap
import com.github.zimablue.devoutserver.api.decouple.map.SingleExecMap
import com.github.zimablue.devoutserver.api.decouple.map.component.Registrable
import com.skillw.pouvoir.internal.core.plugin.PouManagerUtils.getPouManagers
import com.skillw.pouvoir.util.safe
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.function.submitAsync
import java.util.*

class ManagerData(val subPouvoir: SubPouvoir) : KeyMap<String, Manager>(), Registrable<SubPouvoir> {
    private val managers = ArrayList<Manager>()
    val plugin: JavaPlugin = subPouvoir.plugin
    override val key: SubPouvoir = subPouvoir

    override fun put(key: String, value: Manager): Manager? {
        managers.add(value)
        managers.sort()
        return super.put(key, value)
    }

    init {
        for (manager in subPouvoir.getPouManagers()) {
            this.register(manager)
        }
        val dataField = subPouvoir.javaClass.getField("managerData")
        dataField.set(subPouvoir, this)
    }

    override fun register() {
        TotalManager.register(subPouvoir, this)
    }

    fun load() {
        managers.forEach {
            safe(it::onLoad)
        }
    }

    fun enable() {
        managers.forEach {
            safe(it::onEnable)
        }
    }

    fun active() {
        managers.forEach {
            safe(it::onActive)
        }
    }

    private var onReload = SingleExecMap()
    fun reload() {
        submitAsync {
            managers.forEach {
                safe(it::onReload)
            }
            onReload.values.forEach { it() }
        }
    }

    fun onReload(key: String = UUID.randomUUID().toString(), exec: () -> Unit) {
        onReload[key] = exec
    }

    fun disable() {
        managers.forEach {
            safe(it::onDisable)
        }
    }

}