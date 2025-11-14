
import com.example.plugins.configureSerialization
import data.GlucoseRepository
import data.GlucoseTable
import domain.GlucoseService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.testcontainers.containers.PostgreSQLContainer
import presentation.glucoseController

fun Application.testModule(postgres: PostgreSQLContainer<*>) {
    Database.connect(
        url = postgres.jdbcUrl,
        driver = "org.postgresql.Driver",
        user = postgres.username,
        password = postgres.password
    )

    transaction {
        create(GlucoseTable)
    }

    install(Koin) {
        modules(
            module {
                single { GlucoseRepository() }
                single { GlucoseService(get()) }
            }
        )
    }

    configureSerialization()

    val glucoseService by inject<GlucoseService>()

    routing {
        glucoseController(glucoseService)
    }
}
