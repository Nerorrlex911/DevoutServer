package com.github.zimablue.devoutserver.api.plugin

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.server.MinecraftServer
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.extensions.DiscoveredExtension
import java.io.InputStream
import java.net.URL
import java.net.URLClassLoader

class PluginClassLoader(
    name: String,
    urls: Array<URL>? = null,
    parent: ClassLoader? = null,
    val discoveredPlugin: DiscoveredPlugin
) : URLClassLoader(
    "Ext_$name",
    urls,
    parent?: MinecraftServer::class.java.classLoader
) {
    private val children: MutableList<PluginClassLoader> = ArrayList()
    val eventNode: EventNode<Event> by lazy {
        val node = EventNode.all(discoveredPlugin.name!!)
        MinecraftServer.getGlobalEventHandler().addChild(node)
        node
    }
    val logger: ComponentLogger by lazy {
        ComponentLogger.logger(discoveredPlugin.name!!)
    }


    public override fun addURL(url: URL) {
        super.addURL(url)
    }

    fun addChild(loader: PluginClassLoader) {
        children.add(loader)
    }

    @Throws(ClassNotFoundException::class)
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        try {
            return super.loadClass(name, resolve)
        } catch (e: ClassNotFoundException) {
            for (child in children) {
                try {
                    return child.loadClass(name, resolve)
                } catch (ignored: ClassNotFoundException) {
                }
            }
            throw e
        }
    }

    fun getResourceAsStreamWithChildren(name: String): InputStream? {
        val `in` = getResourceAsStream(name)
        if (`in` != null) return `in`

        for (child in children) {
            val childInput = child.getResourceAsStreamWithChildren(name)
            if (childInput != null) return childInput
        }

        return null
    }

    fun terminate() {
        MinecraftServer.getGlobalEventHandler().removeChild(eventNode)
    }
}