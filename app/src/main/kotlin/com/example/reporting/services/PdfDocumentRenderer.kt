package com.example.reporting.services

import com.example.reporting.patterns.ReportPattern
import com.google.gson.Gson
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import form.GlucoseResult
import infrastructure.ResearchResultService
import infrastructure.UserService
import io.ktor.server.util.toLocalDateTime
import io.ktor.util.InternalAPI
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.format.DateTimeFormatter
import java.util.Base64
import java.util.Date
@Suppress("MagicNumber")
class PdfDocumentRenderer(
    private val userService: UserService,
    private val glucoseService: ResearchResultService,
    private val thymeleafService: ThymeleafTemplateRenderer,
) {

//    fun reportPatternParse(string: String): ReportPattern {
//        when (string) {
//            ReportPattern.STANDARD_GLUCOSE.toString() -> {
//                return ReportPattern.STANDARD_GLUCOSE
//            }
//
//            ReportPattern.WEEKLY_GLUCOSE.toString() -> {
//                return ReportPattern.WEEKLY_GLUCOSE
//            }
//
//            ReportPattern.MONTHLY_GLUCOSE.toString() -> {
//                return ReportPattern.MONTHLY_GLUCOSE
//            }
//
//            else -> ReportPattern.DAILY_GLUCOSE_CHANGE
//        }
//        return ReportPattern.STANDARD_GLUCOSE
//    }

    @OptIn(InternalAPI::class)
    suspend fun generatePdf(userId: String, startDate: Date, endDate: Date, reportPattern: ReportPattern): ByteArray =
        when (reportPattern) {
            ReportPattern.STANDARD_GLUCOSE -> generateStandardReport(userId, startDate, endDate)
            ReportPattern.WEEKLY_GLUCOSE -> generateWeeklyReport(userId, startDate, endDate)
            ReportPattern.MONTHLY_GLUCOSE -> generateMonthlyReport(userId, startDate, endDate)
            ReportPattern.DAILY_GLUCOSE_CHANGE -> generateDailyChangeReport(userId, startDate, endDate)
        }

    private suspend fun generateDailyChangeReport(userId: String, startDate: Date, endDate: Date): ByteArray {
        val user = userService.getUser(userId)
        val glucoseResults = glucoseService.getResultsByUserId(userId)
        val gbA1c = glucoseService.getUserGbA1cById(userId)
        val deviation = glucoseService.getDeviationById(userId)

        val chartBase64 = generateGlucoseChartBase64(glucoseResults)

        val html = thymeleafService.render(
            "glucose-report-template.html",
            mapOf(
                "glucose" to glucoseResults,
                "user" to user,
                "startDate" to startDate,
                "endDate" to endDate,
                "chartBase64" to chartBase64,
                "GbA1c" to gbA1c,
                "Deviation" to deviation

            )
        )

        val outputStream = ByteArrayOutputStream()
        PdfRendererBuilder()
            .useFastMode()
            .withHtmlContent(html, null)
            .useFont(File("app/src/main/resources/assets/fonts/Roboto-Regular.ttf"), "Roboto")
            .toStream(outputStream)
            .run()

        return outputStream.toByteArray()
    }

    private suspend fun generateMonthlyReport(userId: String, startDate: Date, endDate: Date): ByteArray {
        val user = userService.getUser(userId)
        val glucoseResults = glucoseService.getResultsByUserId(userId)

        val chartBase64 = generateGlucoseChartBase64(glucoseResults)

        val html = thymeleafService.render(
            "glucose-report-template.html",
            mapOf(
                "glucose" to glucoseResults,
                "user" to user,
                "startDate" to startDate,
                "endDate" to endDate,
                "chartBase64" to chartBase64
            )
        )

        val outputStream = ByteArrayOutputStream()
        PdfRendererBuilder()
            .useFastMode()
            .withHtmlContent(html, null)
            .useFont(File("app/src/main/resources/assets/fonts/Roboto-Regular.ttf"), "Roboto")
            .toStream(outputStream)
            .run()

        return outputStream.toByteArray()
    }

    private suspend fun generateWeeklyReport(userId: String, startDate: Date, endDate: Date): ByteArray {
        val user = userService.getUser(userId)
        val glucoseResults = glucoseService.getResultsByUserId(userId)

        val chartBase64 = generateGlucoseChartBase64(glucoseResults)

        val html = thymeleafService.render(
            "glucose-report-template.html",
            mapOf(
                "glucose" to glucoseResults,
                "user" to user,
                "startDate" to startDate,
                "endDate" to endDate,
                "chartBase64" to chartBase64
            )
        )

        val outputStream = ByteArrayOutputStream()
        PdfRendererBuilder()
            .useFastMode()
            .withHtmlContent(html, null)
            .useFont(File("app/src/main/resources/assets/fonts/Roboto-Regular.ttf"), "Roboto")
            .toStream(outputStream)
            .run()

        return outputStream.toByteArray()
    }

    private suspend fun generateStandardReport(userId: String, startDate: Date, endDate: Date): ByteArray {
        val user = userService.getUser(userId)
        val glucoseResults = glucoseService.getGlucoseResultByIdBetweenDates(userId, startDate, endDate)
        val gbA1c = glucoseService.getUserGbA1cById(userId)
        val deviation = glucoseService.getDeviationById(userId)
        val chartBase64 = generateGlucoseChartBase64(glucoseResults)
        var glcose1: List<GlucoseResult> = emptyList()
        var glcose2: List<GlucoseResult> = emptyList()
        glcose1 = if (glucoseResults.size <= 15) {
            glucoseResults.subList(0, glucoseResults.size)
        } else {
            glucoseResults.subList(0, 15)
        }
        if (glucoseResults.size > 15) {
            glcose2 = glucoseResults.subList(16, glucoseResults.lastIndex + 1)
        }
        val html = thymeleafService.render(
            "glucose-report-template.html",
            mapOf(
                "glucose1" to glcose1,
                "glucose2" to glcose2,
                "user" to user,
                "startDate" to startDate,
                "endDate" to endDate,
                "chartBase64" to chartBase64,
                "GbA1c" to gbA1c,
                "Deviation" to deviation
            )
        )

        val outputStream = ByteArrayOutputStream()
        val baseUri = File("app/src/main/resources/").toURI().toString()

        PdfRendererBuilder()
            .useFastMode()
            .withHtmlContent(html, baseUri)
            .useFont(File("app/src/main/resources/assets/fonts/Roboto-Regular.ttf"), "Roboto")
            .toStream(outputStream)
            .run()
        // informacje commit

        return outputStream.toByteArray()
    }

    @OptIn(InternalAPI::class)
    fun generateGlucoseChartBase64(results: List<GlucoseResult>): String {
        val labels = results.map {
            it.timestamp.toLocalDateTime().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
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
                ),
                "plugins" to mapOf(
                    "datalabels" to mapOf(
                        "display" to true,
                        "anchor" to "top",
                        "color" to "rgba(30, 30, 30, 1.0)",
                        "backgroundColor" to "rgba(75, 192, 192, 0.5)",
                        "borderColor" to "rgba(255, 255, 255, 1.0)",
                        "borderWidth" to 1,
                        "borderRadius" to 2,
                    )
                )
            )
        )

        val gson = Gson()
        val json = gson.toJson(chartConfig)
        val encodedJson = URLEncoder.encode(json, StandardCharsets.UTF_8.toString())
        val chartUrl = "https://quickchart.io/chart?width=800&height=400&c=$encodedJson"

        val imageBytes = URL(chartUrl).readBytes()

        val base64 = Base64.getEncoder().encodeToString(imageBytes)
        return base64
    }
}
