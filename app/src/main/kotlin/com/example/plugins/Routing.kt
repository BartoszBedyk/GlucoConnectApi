package com.example.plugins


import infrastructure.ResearchResultService
import infrastructure.ResearchResultDao
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import javax.sql.DataSource
import rest.researchResultRoutes

fun Application.configureRouting(dataSource: DataSource) {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
        }
    }

    val researchResultDao = ResearchResultDao(dataSource)
    val researchResultService = ResearchResultService(researchResultDao);

    routing {
        researchResultRoutes(researchResultService)

        get("/") {
            call.respondText("Hello World!")
        }

    }
}
