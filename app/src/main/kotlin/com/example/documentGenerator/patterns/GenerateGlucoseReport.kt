package com.example.documentGenerator.patterns

import DateSerializer
import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*


@Serializable
data class GenerateGlucoseReport(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    @Serializable(with = DateSerializer::class)
    val startDate: Date,
    @Serializable(with = DateSerializer::class)
    val endDate: Date,
    val reportPattern: ReportPattern
)
