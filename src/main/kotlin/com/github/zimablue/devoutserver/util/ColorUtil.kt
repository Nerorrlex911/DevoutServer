package com.github.zimablue.devoutserver.util

import java.util.regex.Pattern

fun String.colored() = this.replace("&", "§")

val STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + '§' + "[0-9A-FK-OR]");

fun String.uncolored() = STRIP_COLOR_PATTERN.matcher(this).replaceAll("")