package com.example.documentGenerator.patterns

import kotlinx.serialization.Serializable

@Serializable
enum class ReportPattern {
    STANDARD_GLUCOSE, DAILY_GLUCOSE_CHANGE, MONTHLY_GLUCOSE, WEEKLY_GLUCOSE
}