import io.ktor.client.HttpClient
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
abstract class BaseKtorTest {
    private val postgres = PostgreSQLContainer<Nothing>("postgres:15.5").apply {
        withDatabaseName("testdb")
        withUsername("test")
        withPassword("test")
        start()
    }

    protected fun runTest(block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) = testApplication {
        application {
            testModule(postgres)
        }
        val client = createClient { }
        block(client)
    }

    protected val json = Json { ignoreUnknownKeys = true }
}
