package com.github.zimablue.devoutserver.lang.impl

import com.github.zimablue.devoutserver.lang.LangSection
import com.github.zimablue.devoutserver.util.colored
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import taboolib.common.util.replaceWithOrder

class TextSection(source: Map<String, Any>) : LangSection(source) {

    constructor(text: String) : this(mapOf("text" to text))

    val text = source["text"]?.toString()?.ifEmpty { null }

    override fun toComponent(sender: CommandSender, vararg args: Any): Component? {
        return text?.replaceWithOrder(*args)?.colored()
    }
}