package com.github.zimablue.devoutserver.plugin.lifecycle

import com.github.zimablue.devoutserver.plugin.Plugin
import java.util.LinkedList


class PluginLifeCycleManager(val plugin: Plugin) {
    val lifeCycleTasks = mutableMapOf<Plugin, LinkedList<LifeCycleTask>>()
}