package com.github.zimablue.devoutserver.util

import java.io.File

fun getAllFiles(dir: File): ArrayList<File> {
    val list = ArrayList<File>()
    val files = dir.listFiles() ?: arrayOf<File>()
    for (file: File in files) {
        if (file.isDirectory) {
            list.addAll(getAllFiles(file))
        } else {
            list.add(file)
        }
    }
    return list
}