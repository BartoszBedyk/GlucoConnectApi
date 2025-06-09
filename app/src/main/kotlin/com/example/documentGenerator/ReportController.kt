package com.example.documentGenerator

import com.example.documentGenerator.DocumentService.PdfDocumentRenderer
import com.example.documentGenerator.DocumentService.ThymeleafTemplateRenderer
import com.example.documentGenerator.patterns.GenerateGlucoseReport
import infrastructure.ResearchResultService
import infrastructure.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.reportRoutes(userService: UserService, glucoseService: ResearchResultService, thymeleafTemplateRenderer: ThymeleafTemplateRenderer) {

    val reportService  = PdfDocumentRenderer(
        userService = userService,
        glucoseService = glucoseService,
        thymeleafService = thymeleafTemplateRenderer

    )
    post("/report") {

            val request = call.receive<GenerateGlucoseReport>()
            val pdfBytes = reportService.generatePdf(request.uuid, request.startDate, request.endDate, request.reportPattern)

            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "glucose-report.pdf").toString()
            )

            call.respondBytes(
                bytes = pdfBytes,
                contentType = ContentType.Application.Pdf
            )
        }


}