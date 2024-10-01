package com.github.zimablue.devoutserver.util

import com.github.zimablue.devoutserver.util.ResourceUtils.extractResource
import java.io.File

fun createIfNotExists(name: String, vararg fileNames: String) {
    val dir = File(name)
    if (!dir.exists()) {
        dir.mkdir()
        for (fileName in fileNames) {
            safe { extractResource("$name/$fileName") }
        }
    }
}