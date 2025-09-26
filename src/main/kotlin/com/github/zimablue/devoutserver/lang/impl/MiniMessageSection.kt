package com.github.zimablue.devoutserver.lang.impl

import com.github.zimablue.devoutserver.lang.LangSection
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minestom.server.command.CommandSender
import taboolib.common.util.replaceWithOrder

class MiniMessageSection(source: Map<String, Any>) : LangSection(source) {
    val text = source["text"]?.toString()?.ifEmpty { null }
    override fun toComponent(sender: CommandSender, vararg args: Any): Component? {
        return text?.replaceWithOrder(*args)?.let { MiniMessage.miniMessage().deserialize(it) }
    }
}