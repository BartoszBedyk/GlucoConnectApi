package com.example.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

fun Application.configureDatabases(): HikariDataSource {
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/postgres"
        username = System.getenv("DB_USER") ?: "root"
        password = System.getenv("DB_PASSWORD") ?: "Postgres1"
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = 10
        minimumIdle = 2
        idleTimeout = 60000
        connectionTimeout = 30000
    }

    val dataSource = HikariDataSource(hikariConfig)
    return dataSource
}

private fun runLiquibaseMigrations(dataSource: HikariDataSource) {
    dataSource.connection.use { connection ->
        val jdbcConnection = JdbcConnection(connection)
        val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection)
        val liquibase = Liquibase(
            "db/db.changelog-master.yaml",
            ClassLoaderResourceAccessor(),
            database
        )
        liquibase.update("development")
    }
}


