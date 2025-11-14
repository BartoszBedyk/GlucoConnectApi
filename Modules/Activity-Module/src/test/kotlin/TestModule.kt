
import com.example.plugins.configureSerialization
import data.ActivityRepository
import data.ActivityTable
import domain.ActivityService
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
import presentation.activityController

fun Application.testModule(postgres: PostgreSQLContainer<*>) {
    Database.connect(
        url = postgres.jdbcUrl,
        driver = "org.postgresql.Driver",
        user = postgres.username,
        password = postgres.password
    )

    transaction {
        create(ActivityTable)
    }

    install(Koin) {
        modules(
            module {
                single { ActivityRepository() }
                single { ActivityService(get()) }
            }
        )
    }

    configureSerialization()

    val activityService by inject<ActivityService>()

    routing {
        activityController(activityService)
    }
}
