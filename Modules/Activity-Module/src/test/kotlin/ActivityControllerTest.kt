import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import model.ActivityEntity
import model.CreateActivityRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.Test
import kotlin.test.assertTrue

@Testcontainers
class ActivityControllerTest : ActivityTestStubs() {
    private val postgres = PostgreSQLContainer<Nothing>("postgres:15.5").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
        start()
    }

    @Test
    fun `POST activities should create activity`() = testApplication {
        application {
            testModule(postgres)
        }

        val client = createClient { }

        val response = client.post("/activities") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jsonPost)
        }
        val id = response.bodyAsText().toInt()

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(1, id)
    }

    @Test
    fun `GET activities should return the activity that was created`() = testApplication {
        application {
            testModule(postgres)
        }

        val client = createClient { }

        val inputActivity = Json.decodeFromString<CreateActivityRequest>(jsonPost)

        val postResponse = client.post("/activities") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jsonPost)
        }

        assertEquals(HttpStatusCode.Created, postResponse.status)

        val getResponse = client.get("/activities") {
            accept(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, getResponse.status)

        val responseBody = getResponse.bodyAsText()
        println("GET response: $responseBody")

        val activities = Json.decodeFromString<List<ActivityEntity>>(responseBody)

        assertTrue(activities.isNotEmpty(), "Response should contain at least one activity")

        val created = activities.first()

        assertEquals(inputActivity.value, created.value)
        assertEquals(inputActivity.userId, created.userId)
        assertNotNull(created.id)
        assertNotNull(created.createdAt)
    }

    @Test
    fun `GET activity by id should return the created activity`() = testApplication {
        application {
            testModule(postgres)
        }

        val client = createClient { }

        val inputActivity = Json.decodeFromString<CreateActivityRequest>(jsonPost)

        val postResponse = client.post("/activities") {
            contentType(ContentType.Application.Json)
            setBody(jsonPost)
        }
        assertEquals(HttpStatusCode.Created, postResponse.status)

        val createdId = postResponse.bodyAsText().toInt()

        val getResponse = client.get("/activities/$createdId") {
            accept(ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.OK, getResponse.status)
        val activity = Json.decodeFromString<ActivityEntity>(getResponse.bodyAsText())

        assertEquals(createdId, activity.id)
        assertEquals(inputActivity.value, activity.value)
        assertEquals(inputActivity.userId, activity.userId)
        assertNotNull(activity.createdAt)
    }

    @Test
    fun `GET activities by userId should return all activities for that user`() = testApplication {
        application {
            testModule(postgres)
        }

        val client = createClient { }
        val userId = 1

        val activityRunning = Json.decodeFromString<CreateActivityRequest>(jsonRunning)
        val activityStopping = Json.decodeFromString<CreateActivityRequest>(jsonStopping)

        client.post("/activities") {
            contentType(ContentType.Application.Json)
            setBody(jsonRunning)
        }
        client.post("/activities") {
            contentType(ContentType.Application.Json)
            setBody(jsonStopping)
        }

        val getResponse = client.get("/activities/user/$userId") {
            accept(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.OK, getResponse.status)

        val activities = Json.decodeFromString<List<ActivityEntity>>(getResponse.bodyAsText())
        assertEquals(2, activities.size)
        assertTrue(activities.any { it.value == activityRunning.value })
        assertTrue(activities.any { it.value == activityStopping.value })

        activities.forEach {
            assertEquals(userId, it.userId)
            assertNotNull(it.id)
            assertNotNull(it.createdAt)
        }
    }
}
