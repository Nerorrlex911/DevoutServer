package com.github.zimablue.devoutserver.api.decouple

import com.github.zimablue.devoutserver.api.manager.ManagerData
import com.github.zimablue.devoutserver.api.decouple.map.component.Registrable

interface SubPouvoir : Registrable<String> {
    var managerData: ManagerData

    override fun register() {
        TotalManager.register(this.managerData)
    }

    fun reload() {
        managerData.reload()
    }
}