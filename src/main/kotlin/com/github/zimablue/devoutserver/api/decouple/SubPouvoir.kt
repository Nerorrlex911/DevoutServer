package com.github.zimablue.devoutserver.api.decouple

import com.github.zimablue.devoutserver.api.manager.ManagerData
import com.github.zimablue.devoutserver.api.decouple.map.component.Registrable
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.function.console
import taboolib.module.lang.sendLang

interface SubPouvoir : Registrable<String> {
    var managerData: ManagerData
    val plugin: JavaPlugin


    fun load() {
        managerData.load()
        console().sendLang("decouple-load", key)
    }

    fun enable() {
        managerData.enable()
        console().sendLang("decouple-enable", key)
    }

    fun active() {
        managerData.active()
    }

    fun disable() {
        managerData.disable()
        console().sendLang("decouple-disable", key)
    }

    override fun register() {
        TotalManager.register(this.managerData)
    }

    fun reload() {
        managerData.reload()
    }
}