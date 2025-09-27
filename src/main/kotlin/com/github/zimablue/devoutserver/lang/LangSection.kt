package com.github.zimablue.devoutserver.lang

import com.github.zimablue.devoutserver.lang.impl.MiniMessageSection
import com.github.zimablue.devoutserver.lang.impl.TextSection
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.minestom.server.command.CommandSender

abstract class LangSection(val source: Map<String,Any>) {

    /**
     * 将语言文本转换为组件，一些格式节点如TextSection(旧版格式)和MiniMessageSection(MiniMessage格式)会覆写此函数
     */
    abstract fun toComponent(sender: CommandSender, vararg args: Any): Component?

    /**
     * 将语言文本转换为纯文本
     */
    open fun toText(sender: CommandSender, vararg args: Any): String? {
        return toComponent(sender, *args)?.let { PlainTextComponentSerializer.plainText().serialize(it) }
    }

    /**
     * 发送语言文本给命令发送者
     * 一些特殊信息如action bar或title会覆写此函数
     */
    open fun send(sender: CommandSender, vararg args: Any) {
        toComponent(sender, *args)?.let { sender.sendMessage(it) }
    }

    companion object {
        fun create(type: String, source: Map<String, Any>): LangSection? {
            return when(type.lowercase()) {
                "text" -> TextSection(source)
                "minimessage" -> MiniMessageSection(source)
                else -> null
            }
        }
    }
}