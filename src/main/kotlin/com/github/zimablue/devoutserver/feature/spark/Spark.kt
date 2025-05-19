package com.github.zimablue.devoutserver.feature.spark

import com.github.zimablue.devoutserver.lifecycle.Awake
import com.github.zimablue.devoutserver.lifecycle.AwakePriority
import com.github.zimablue.devoutserver.lifecycle.LifeCycle
import me.lucko.spark.minestom.SparkMinestom
import net.minestom.server.command.ConsoleSender
import org.slf4j.LoggerFactory
import java.nio.file.Path


object Spark {
    val directory: Path = Path.of("spark")
    val logger = LoggerFactory.getLogger(Spark::class.java)
    val spark by lazy {
        SparkMinestom.builder(directory)
            .logger(logger)
            .commands(true) // enables registration of Spark commands
            .permissionHandler { sender, permission ->
                sender is ConsoleSender
            } // allows only console senders to execute all commands
            .enable()
    }
    init {
        val version = spark.platform().plugin.version
        logger.info("Spark(version: $version) started")
    }
    @Awake(LifeCycle.SHUTDOWN,AwakePriority.NORMAL)
    fun onStop() {
        spark.shutdown()
    }
}