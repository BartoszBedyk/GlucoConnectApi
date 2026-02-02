package com.example.glucoses

import com.example.BaseKtorTest
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.Base64
import java.util.UUID
import kotlin.test.Test

@Testcontainers
class GlucoseControllerTest :
    BaseKtorTest(),
    GlucoseTestStubs {

    val credentials = Base64.getEncoder()
        .encodeToString("test:test".toByteArray())

    @Test
    fun `POST glucoses should create glucose result`() = runTest { client ->

        val response = client.post("/glucoses") {
            header(HttpHeaders.Authorization, "Basic $credentials")
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jsonPost)
        }

        println(response.status)
        println(response.bodyAsText())

        assertEquals(HttpStatusCode.Created, response.status)

        val id = response.bodyAsText().removeSurrounding("\"")

        assertDoesNotThrow {
            UUID.fromString(id)
        }
    }
}
