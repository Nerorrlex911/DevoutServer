package com.github.zimablue.devoutserver.util


import com.github.zimablue.devoutserver.util.ClassUtil.instance
import com.github.zimablue.devoutserver.util.ClassUtil.isSingleton
import java.lang.reflect.Method

fun Method.execute(vararg args: Any) {
    this.declaringClass.instance?.let {
        this.invoke(it, *args)
        return
    }
    this.invoke(null, *args)
}