package com.example.glucoses

import com.example.BaseKtorTest
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import model.CreateGlucoseRequest
import model.GlucoseEntity
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.testcontainers.junit.jupiter.Testcontainers
import pageable.PageResponse
import runSql
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

    @Test
    fun `GET glucoses should get all user results`() = runTest { client ->
        val inputGlucose = testJson.decodeFromString<CreateGlucoseRequest>(jsonPost)

        runSql("glucoses/create-glucose.sql")

        val getResponse = client.get("/glucoses/user") {
            header(HttpHeaders.Authorization, "Basic $credentials")
            accept(ContentType.Application.Json)
        }

        Assertions.assertEquals(HttpStatusCode.OK, getResponse.status)

        val responseBody = getResponse.bodyAsText()
        println("GET response: $responseBody")

        val heartbeats = testJson.decodeFromString<PageResponse<GlucoseEntity>>(responseBody)

        assertTrue(heartbeats.content.isNotEmpty(), "Response should contain at least one heartbeat")

        val retrived = heartbeats.content.first()

        assertTrue(inputGlucose.concentration == retrived.concentration)
        assertEquals(inputGlucose.unit, retrived.unit)
        assertEquals(inputGlucose.timestamp, retrived.timestamp)
        assertEquals(inputGlucose.note, inputGlucose.note)
        assertEquals(inputGlucose.afterMeal, retrived.afterMeal)
    }

    @Test
    fun `GET glucoses id should get glucose by id`() = runTest { client ->
        val inputGlucose = testJson.decodeFromString<CreateGlucoseRequest>(jsonPost)

        runSql("glucoses/create-glucose.sql")

        val getResponse = client.get("/glucoses/$glucoseId") {
            header(HttpHeaders.Authorization, "Basic $credentials")
            accept(ContentType.Application.Json)
        }

        Assertions.assertEquals(HttpStatusCode.OK, getResponse.status)

        val responseBody = getResponse.bodyAsText()
        println("GET response: $responseBody")

        val heartbeat = testJson.decodeFromString<GlucoseEntity>(responseBody)

        assertNotNull(heartbeat, "Response should contain at least one glucose")

        assertTrue(inputGlucose.concentration == heartbeat.concentration)
        assertEquals(inputGlucose.unit, heartbeat.unit)
        assertEquals(inputGlucose.timestamp, heartbeat.timestamp)
        assertEquals(inputGlucose.note, heartbeat.note)
        assertEquals(inputGlucose.afterMeal, heartbeat.afterMeal)
    }
}
