
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import java.io.File
import javax.sql.DataSource
import kotlin.test.Test

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.postgresql.ds.PGSimpleDataSource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class ActivityControllerIntegrationTest {
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
        val jsonBody = File("src/test/resources/activity_create_input.json").readText()

        val response = client.post("/activities") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jsonBody)
        }
        val id = response.bodyAsText().toInt()

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(1, id)
    }
}
