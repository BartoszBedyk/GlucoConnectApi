package com.example.documentGenerator.DocumentService

import com.google.gson.Gson
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import form.GlucoseResult
import infrastructure.ResearchResultService
import infrastructure.UserService
import io.ktor.server.util.*
import io.ktor.util.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.util.*


class PdfDocumentRenderer
    (
    private val userService: UserService,
    private val glucoseService: ResearchResultService,
    private val thymeleafService: ThymeleafTemplateRenderer,
) {


    @OptIn(InternalAPI::class)
    suspend fun generatePdf(userId: String, startDate: Date, endDate: Date): ByteArray {
        val user = userService.getUser(userId)
        val glucoseResults = glucoseService.getResultsByUserId(userId)

        val chartBase64 = generateGlucoseChartBase64(glucoseResults)

        val html = thymeleafService.render("glucose-report-template.html", mapOf(
            "glucose" to glucoseResults,
            "user" to user,
            "startDate" to startDate,
            "endDate" to endDate,
            "chartBase64" to chartBase64
        ))

        val outputStream = ByteArrayOutputStream()
        PdfRendererBuilder()
            .useFastMode()
            .withHtmlContent(html, null)
            .useFont(File("app/src/main/resources/assets/fonts/Roboto-Regular.ttf"), "Roboto")
            .toStream(outputStream)
            .run()

        return outputStream.toByteArray()
    }

    @OptIn(InternalAPI::class)
    fun generateGlucoseChartBase64(results: List<GlucoseResult>): String {
        val labels = results.map { it.timestamp.toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) }
        val values = results.map { it.glucoseConcentration }

        val chartConfig = mapOf(
            "type" to "line",
            "data" to mapOf(
                "labels" to labels,
                "datasets" to listOf(
                    mapOf(
                        "label" to "Poziom glukozy",
                        "data" to values,
                        "borderColor" to "rgb(75, 192, 192)",
                        "fill" to false,
                        "tension" to 0.1
                    )
                )
            ),
            "options" to mapOf(
                "scales" to mapOf(
                    "x" to mapOf("title" to mapOf("display" to true, "text" to "Czas")),
                    "y" to mapOf("title" to mapOf("display" to true, "text" to "mg/dL"))
                )
            )
        )

        // Serializacja do JSON i kodowanie do URL
        val gson = Gson()
        val json = gson.toJson(chartConfig)
        val encodedJson = URLEncoder.encode(json, StandardCharsets.UTF_8.toString())
        val chartUrl = "https://quickchart.io/chart?width=800&height=400&c=$encodedJson"

        val imageBytes = URL(chartUrl).readBytes()

        val base64 = Base64.getEncoder().encodeToString(imageBytes)
        return base64
    }

}