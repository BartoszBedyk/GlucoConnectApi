package com.example.api.reserchResults.domain.form

import DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ResearchResultForm(
    val sequenceNumber: Int,
    val glucoseConcentration: Double,
    val unit: String,
    @Serializable(with = DateSerializer::class) val timestamp: Date
)
