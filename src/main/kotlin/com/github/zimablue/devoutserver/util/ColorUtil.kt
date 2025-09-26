package com.github.zimablue.devoutserver.util

import dev.vankka.enhancedlegacytext.EnhancedLegacyText
import net.minestom.server.command.CommandSender
import java.util.regex.Pattern

fun CommandSender.sendColored(vararg message: String) {
    message.forEach {
        this.sendMessage(it.colored())
    }
}

fun String.colored() = EnhancedLegacyText.get().buildComponent(this).build()

val STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]");

fun String.uncolored() = STRIP_COLOR_PATTERN.matcher(this).replaceAll("")