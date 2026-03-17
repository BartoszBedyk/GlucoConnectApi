package com.example

import form.UserCredentials
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.junit.Assert.assertEquals
import org.junit.Test

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

    @Test
    fun generatePDF() = testApplication {
        application { module() }
    }
}
