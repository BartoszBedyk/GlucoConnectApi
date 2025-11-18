import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.junit.Assert.assertEquals
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.UUID
import kotlin.test.Test

@Testcontainers
class GlucoseControllerTest :
    BaseKtorTest(),
    GlucoseTestStubs {

    @Test
    fun `POST glucoses should create glucose result`() = runTest { client ->
        val response = client.post("/glucoses") {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
            setBody(jsonPost)
        }

        val id = response.bodyAsText()
        val noBrackets: String = id.removeSurrounding("\"")

        assertEquals(HttpStatusCode.Created, response.status)
        val uuid = UUID.fromString(noBrackets)
    }
}
