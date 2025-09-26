package com.github.zimablue.devoutserver.lang

import com.github.zimablue.devoutserver.lang.impl.MiniMessageSection
import com.github.zimablue.devoutserver.lang.impl.TextSection
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.command.CommandSender

abstract class LangSection(val source: Map<String,Any>) {

    abstract fun toComponent(sender: CommandSender, vararg args: Any): Component?

    open fun toText(sender: CommandSender, vararg args: Any): String? {
        return toComponent(sender, *args)?.let { PlainTextComponentSerializer.plainText().serialize(it) }
    }

    open fun send(sender: CommandSender, vararg args: Any) {
        toComponent(sender, *args)?.let { sender.sendMessage(it) }
    }

    companion object {
        fun create(type: String, source: Map<String, Any>): LangSection? {
            return when(type) {
                "text" -> TextSection(source)
                "minimessage" -> MiniMessageSection(source)
                else -> null
            }
        }
    }
}