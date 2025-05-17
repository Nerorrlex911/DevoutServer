package com.github.zimablue.devoutserver.util

import com.github.zimablue.devoutserver.DevoutServer
import java.io.File

fun getAllFiles(dir: File): ArrayList<File> {
    val list = ArrayList<File>()
    val files = dir.canonicalFile.listFiles() ?: arrayOf<File>()
    DevoutServer.LOGGER.info("Getting all script files ${files.joinToString { it.path }}")
    for (file: File in files) {
        if (file.isDirectory) {
            list.addAll(getAllFiles(file))
        } else {
            list.add(file)
        }
    }
    return list
}