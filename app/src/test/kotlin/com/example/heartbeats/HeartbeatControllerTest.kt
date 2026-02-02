package com.example.heartbeats

import com.example.BaseKtorTest
import heartbeats.HeartbeatTestStubs
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
import model.CreateHeartbeatRequest
import model.HeartbeatEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import pageable.PageResponse
import runSql
import java.util.Base64
import java.util.UUID

@Testcontainers
class HeartbeatControllerTest :
    BaseKtorTest(),
    HeartbeatTestStubs {

    private val credentials = Base64.getEncoder()
        .encodeToString("test:test".toByteArray())

    @Test
    fun `POST heartbeats should create heartbeat result`() = runTest { client ->
        val response = client.post("/heartbeats") {
            header(HttpHeaders.Authorization, "Basic $credentials")
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jsonPost)
        }

        val id = response.bodyAsText()
        val noBrackets: String = id.removeSurrounding("\"")

        assertEquals(HttpStatusCode.Created, response.status)
        val uuid = UUID.fromString(noBrackets)
    }

    @Test
    fun `GET heartbeats should return the heartbeat for user`() = runTest { client ->
        val inputHeartbeat = testJson.decodeFromString<CreateHeartbeatRequest>(jsonPost)

        runSql("heartbeats/create-heartbeat.sql")

        val getResponse = client.get("/heartbeats/user") {
            header(HttpHeaders.Authorization, "Basic $credentials")
            accept(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, getResponse.status)

        val responseBody = getResponse.bodyAsText()
        println("GET response: $responseBody")

        val heartbeats = testJson.decodeFromString<PageResponse<HeartbeatEntity>>(responseBody)

        assertTrue(heartbeats.content.isNotEmpty(), "Response should contain at least one heartbeat")

        val retrived = heartbeats.content.first()

        assertEquals(inputHeartbeat.systolicPressure, retrived.systolicPressure)
        assertEquals(inputHeartbeat.diastolicPressure, retrived.diastolicPressure)
        assertEquals(inputHeartbeat.pulse, retrived.pulse)
        assertNotNull(retrived.id)
        assertNotNull(retrived.createdAt)
    }

    @Test
    fun `GET heartbeats id should return the heartbeat for delivered id`() = runTest { client ->
        val inputHeartbeat = testJson.decodeFromString<CreateHeartbeatRequest>(jsonPost)

        runSql("heartbeats/create-heartbeat.sql")

        val getResponse = client.get("/heartbeats/$heartbeatId") {
            header(HttpHeaders.Authorization, "Basic $credentials")
            accept(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, getResponse.status)

        val responseBody = getResponse.bodyAsText()
        println("GET response: $responseBody")

        val heartbeat = testJson.decodeFromString<HeartbeatEntity>(responseBody)

        assertNotNull(heartbeat, "Response should contain exactly one heartbeat")

        assertEquals(inputHeartbeat.systolicPressure, heartbeat.systolicPressure)
        assertEquals(inputHeartbeat.diastolicPressure, heartbeat.diastolicPressure)
        assertEquals(inputHeartbeat.pulse, heartbeat.pulse)
        assertNotNull(heartbeat.id)
        assertNotNull(heartbeat.createdAt)
    }
}
