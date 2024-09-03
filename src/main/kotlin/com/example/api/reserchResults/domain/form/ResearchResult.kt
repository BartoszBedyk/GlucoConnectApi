package com.example.api.reserchResults.domain.form

import DateSerializer
import UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ResearchResult(
    @Serializable(with = UUIDSerializer::class)
    val Id : UUID,
    val SequenceNumber: Int,
    val GlucoseConcentration : Double,
    val Unit : String,
    @Serializable(with = DateSerializer::class) val timestamp: Date
)
