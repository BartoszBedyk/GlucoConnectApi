
import com.example.plugins.configureSerialization
import data.HeartbeatRepository
import data.HeartbeatTable
import domain.HeartbeatService
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
import presentation.heartbeatController

fun Application.testModule(postgres: PostgreSQLContainer<*>) {
    Database.connect(
        url = postgres.jdbcUrl,
        driver = "org.postgresql.Driver",
        user = postgres.username,
        password = postgres.password
    )

    transaction {
        create(HeartbeatTable)
    }

    install(Koin) {
        modules(
            module {
                single { HeartbeatRepository() }
                single { HeartbeatService(get()) }
            }
        )
    }

    configureSerialization()

    val heartbeatService by inject<HeartbeatService>()

    routing {
        heartbeatController(heartbeatService)
    }
}
