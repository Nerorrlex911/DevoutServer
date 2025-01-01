package com.github.zimablue.devoutserver.util.map

/**
 * Multi exec map
 *
 * @constructor Create empty Multi exec map
 */
open class MultiExecMap : LowerMap<SingleExecMap>() {
    /**
     * Run
     *
     * @param thing
     */
    fun run(key: String) {
        filter { it.key == key.lowercase() }.forEach { it.value.invoke() }
    }
}