package com.github.zimablue.devoutserver.util.map.component

/**
 * @className Registrable
 *
 * @author Glom
 * @date 2022/7/18 7:56 Copyright 2022 user.
 *
 * 可注册的类
 */
interface Registrable<K> : Keyable<K> {
    fun register()
}