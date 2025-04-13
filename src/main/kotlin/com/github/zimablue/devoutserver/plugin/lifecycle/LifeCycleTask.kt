package com.github.zimablue.devoutserver.plugin.lifecycle

import com.github.zimablue.devoutserver.lifecycle.AwakePriority

class LifeCycleTask(val priority: AwakePriority, val callback: Runnable) {
}