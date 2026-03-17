package com.example

import customSerializersModule
import data.AuthenticationTable
import data.GlucoseTable
import data.HeartbeatTable
import data.UserTable
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import runSql

@Testcontainers
abstract class BaseKtorTest {

    protected val testJson = Json {
        serializersModule = customSerializersModule
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    companion object {
        val postgres = PostgreSQLContainer("postgres:15.5").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
            start()
        }

        @JvmStatic
        @BeforeAll
        fun initDb() {
            Database.connect(
                url = postgres.jdbcUrl,
                driver = "org.postgresql.Driver",
                user = postgres.username,
                password = postgres.password
            )

            transaction {
                SchemaUtils.create(
                    UserTable,
                    AuthenticationTable,
                    HeartbeatTable,
                    GlucoseTable
                )
            }
        }
    }

    protected fun runTest(block: suspend ApplicationTestBuilder.(HttpClient) -> Unit) = testApplication {
        application {
            testModule()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(
                    testJson
                )
            }
        }

        runSql("create-user.sql")
        try {
            block(client)
        } finally {
            runSql("delete-user.sql")
        }
    }
}
