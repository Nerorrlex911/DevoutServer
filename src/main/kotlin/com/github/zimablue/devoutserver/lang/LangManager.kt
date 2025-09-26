package com.github.zimablue.devoutserver.lang

import com.github.zimablue.devoutserver.util.map.BaseMap
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import java.io.File

open class LangManager : BaseMap<String,LangFile>() {

    fun init(file: File) : LangManager {
        clear()
        val files = file.canonicalFile.listFiles() ?: arrayOf<File>()
        for (f in files) {
            if (f.isFile && f.extension == "yml") {
                val langFile = LangFile(f)
                val name = f.nameWithoutExtension
                put(name, langFile)
            }
        }
        return this
    }

    fun sendLang(sender: CommandSender, key: String, vararg args: Any) {
        val sections = get(sender.getLocale())?.get(key)?:run {
            sender.sendMessage("{$key}")
            return
        }
        sections.forEach { section ->
            section.send(sender, *args)
        }
    }
    fun asLangComponent(sender: CommandSender, key: String, vararg args: Any): List<Component> {
        val sections = get(sender.getLocale())?.get(key)?:run{
            return listOf(Component.text("{$key}"))
        }
        return sections.mapNotNull { section ->
            section.toComponent(sender, *args)
        }
    }

    companion object {
        /** 语言文件代码转换 */
        val languageCodeTransfer = hashMapOf(
            "zh_hans_cn" to "zh_CN",
            "zh_hant_cn" to "zh_TW",
            "en_ca" to "en_US",
            "en_au" to "en_US",
            "en_gb" to "en_US",
            "en_nz" to "en_US"
        )
        fun CommandSender.getLocale(): String {
            return if (this is net.minestom.server.entity.Player) {
                val locale = this.locale.displayName
                languageCodeTransfer[locale.lowercase()] ?: locale
            } else {
                return "zh_CN"
            }
        }
    }
}