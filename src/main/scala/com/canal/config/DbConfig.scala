package com.canal.config

import com.typesafe.config.ConfigFactory

object DbConfig {
    private val config = ConfigFactory.load()

    val PARALLELISM = config.getInt("db-config.parallelism")
    val GROUP_SIZE = config.getInt("db-config.groupSize")
}