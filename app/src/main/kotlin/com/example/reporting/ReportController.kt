package com.example.reporting

import com.example.reporting.patterns.GenerateGlucoseReport
import com.example.reporting.services.PdfDocumentRenderer
import com.example.reporting.services.ThymeleafTemplateRenderer
import infrastructure.ResearchResultService
import infrastructure.UserService
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.reportRoutes(
    userService: UserService,
    glucoseService: ResearchResultService,
    thymeleafTemplateRenderer: ThymeleafTemplateRenderer
) {
    val reportService = PdfDocumentRenderer(
        userService = userService,
        glucoseService = glucoseService,
        thymeleafService = thymeleafTemplateRenderer

    )
    post("/report") {
        val request = call.receive<GenerateGlucoseReport>()
        val pdfBytes =
            reportService.generatePdf(
                request.uuid.toString(),
                request.startDate,
                request.endDate,
                request.reportPattern
            )

        call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "glucose-report.pdf",)
                .toString()
        )

        call.respondBytes(
            bytes = pdfBytes,
            contentType = ContentType.Application.Pdf
        )
    }
}
