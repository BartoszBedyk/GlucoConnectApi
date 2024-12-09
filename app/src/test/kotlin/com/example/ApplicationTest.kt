package com.example

import com.example.plugins.*
import form.UserCredentials
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testLoginAndSecureEndpoint() = testApplication {
        application { module() }

        var credentials = UserCredentials("b.ghj43@wp.pl", "Test123")
        // Test login
        val loginResponse = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody(credentials)
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val token = loginResponse.bodyAsText() // Pobierz token z odpowiedzi

        // Test secure endpoint
        val secureResponse = client.get("/secure-endpoint") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, secureResponse.status)
    }
}

