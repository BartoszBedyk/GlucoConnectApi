import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import model.CreateHeartbeatRequest
import model.HeartbeatEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.testcontainers.junit.jupiter.Testcontainers
import pageable.PageResponse
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertTrue

@Testcontainers
class HeartbeatControllerTest :
    BaseKtorTest(),
    HeartbeatTestStubs {

    @Test
    fun `POST heartbeats should create heartbeat result`() = runTest { client ->
        val response = client.post("/heartbeats") {
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
    fun `GET heartbeats should return the activity that was created`() = runTest { client ->
        val inputHeartbeat = Json.decodeFromString<CreateHeartbeatRequest>(jsonPost)

        val postResponse = client.post("/heartbeats") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jsonPost)
        }

        assertEquals(HttpStatusCode.Created, postResponse.status)

        val getResponse = client.get("/heartbeats/user/userId") {
            accept(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, getResponse.status)

        val responseBody = getResponse.bodyAsText()
        println("GET response: $responseBody")

        val heartbeats = Json.decodeFromString<PageResponse<HeartbeatEntity>>(responseBody)

        assertTrue(heartbeats.content.isNotEmpty(), "Response should contain at least one heartbeat")

        val created = heartbeats.content.first()

        assertEquals(inputHeartbeat.systolicPressure, created.systolicPressure)
        assertEquals(inputHeartbeat.diastolicPressure, created.diastolicPressure)
        assertEquals(inputHeartbeat.pulse, created.pulse)
        assertNotNull(created.id)
        assertNotNull(created.createdAt)
    }
}
