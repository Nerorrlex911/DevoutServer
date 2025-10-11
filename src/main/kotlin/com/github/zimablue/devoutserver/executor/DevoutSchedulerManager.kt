package com.github.zimablue.devoutserver.executor

import kotlinx.coroutines.*
import net.minestom.server.ServerFlag
import net.minestom.server.timer.Scheduler
import java.util.*

object DevoutSchedulerManager {
    private val TICK_TIME_NANOS: Long = 1000000000L / ServerFlag.SERVER_TICKS_PER_SECOND
    // Windows has an issue with periodically being unable to sleep for < ~16ms at a time
    private val SLEEP_THRESHOLD: Long = (if (System.getProperty("os.name", "")
            .lowercase(Locale.getDefault()).startsWith("windows")
    ) 17 else 2).toLong()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val scheduler: Scheduler = Scheduler.newScheduler()

    fun init() {
        scope.launch {
            var ticks: Long = 0
            var baseTime = System.nanoTime()
            while (isActive) {
                try {
                    scheduler.processTick()
                    scheduler.processTickEnd()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

                ticks++
                val nextTickTime = baseTime + ticks * TICK_TIME_NANOS
                waitUntilNextTick(nextTickTime)
                // Check if the server can not keep up with the tickrate
                // if it gets too far behind, reset the ticks & baseTime
                // to avoid running too many ticks at once
                if (System.nanoTime() > nextTickTime + TICK_TIME_NANOS * ServerFlag.SERVER_MAX_TICK_CATCH_UP) {
                    baseTime = System.nanoTime()
                    ticks = 0
                }
            }
        }
    }

    fun stop() {
        scope.cancel()
    }

    private suspend fun waitUntilNextTick(nextTickTimeNanos: Long) {
        var currentTime: Long
        while ((System.nanoTime().also { currentTime = it }) < nextTickTimeNanos) {
            val remainingTime = nextTickTimeNanos - currentTime
            // Sleep less the closer we are to the next tick
            val remainingMilliseconds = remainingTime / 1000000L
            if (remainingMilliseconds >= SLEEP_THRESHOLD) {
                delay(remainingMilliseconds / 2)
            }
        }
    }

}