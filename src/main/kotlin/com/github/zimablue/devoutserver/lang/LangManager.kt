package com.github.zimablue.devoutserver.lang

import com.github.zimablue.devoutserver.util.map.BaseMap
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import java.io.File

open class LangManager : BaseMap<String,LangFile>() {

    fun reload(file: File) : LangManager {
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

    /**
     * 发送语言文本给命令发送者
     * 如果找不到对应语言文件或键值，则发送{key}
     * @param sender 命令发送者
     * @param key 语言键值
     * @param args 替换参数，以{0},{1}...表示
     */
    fun sendLang(sender: CommandSender, key: String, vararg args: Any) {
        val sections = get(sender.getLocale())?.get(key)?:run {
            sender.sendMessage("{$key}")
            return
        }
        sections.forEach { section ->
            section.send(sender, *args)
        }
    }
    /**
     * 根据命令发送者获取组件
     * 如果找不到对应语言文件或键值，则发送{key}
     * @param sender 命令发送者
     * @param key 语言键值
     * @param args 替换参数，以{0},{1}...表示
     * @return 组件列表,可能为单元素列表或空列表
     */
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
                val locale = this.locale.toString()
                languageCodeTransfer[locale.lowercase()] ?: locale
            } else {
                return "zh_CN"
            }
        }
    }
}