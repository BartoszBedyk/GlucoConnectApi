package com.example.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import data.ActivityTable
import data.GlucoseTable
import data.UserTable
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
@Suppress("MagicNumber")
fun Application.configureDatabases(): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/postgres?currentSchema=glucoconnectapi"
        username = System.getenv("DB_USER") ?: "root"
        password = System.getenv("DB_PASSWORD") ?: "Postgres1"
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
        minimumIdle = 2
        idleTimeout = 60000
        connectionTimeout = 30000
    }
    val dataSource = HikariDataSource(hikariConfig)

    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(ActivityTable, GlucoseTable, UserTable)
    }

    return dataSource
}
