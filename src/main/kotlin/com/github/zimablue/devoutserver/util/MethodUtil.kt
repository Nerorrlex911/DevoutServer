package com.github.zimablue.devoutserver.util


import com.github.zimablue.devoutserver.util.ClassUtil.instance
import com.github.zimablue.devoutserver.util.ClassUtil.isSingleton
import java.lang.reflect.Method

fun Method.execute(vararg args: Any) {
    if (this.declaringClass.isSingleton()) {
        this.invoke(this.declaringClass.instance, *args)
        return
    }
    this.invoke(null, *args)
}