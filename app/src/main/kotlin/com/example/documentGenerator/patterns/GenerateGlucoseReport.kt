package com.example.documentGenerator.patterns

import DateSerializer
import UUIDSerializer
import kotlinx.serialization.Serializable
import java.sql.Timestamp
import java.util.*

@Serializable
data class GenerateGlucoseReport(
    val uuid: String,
    @Serializable(with = DateSerializer::class)
    val startDate: Date,
    @Serializable(with = DateSerializer::class)
    val endDate: Date,
    val reportPattern: ReportPattern
)
