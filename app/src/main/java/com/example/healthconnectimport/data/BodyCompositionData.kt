package com.example.healthconnectimport.data

import java.time.ZonedDateTime

data class BodyCompositionData(
    val time: ZonedDateTime,
    val weight: Double,          // kg
    val height: Double,          // cm
    val bmi: Double,            // calculated value
    val fatRate: Double,        // percentage (0-100)
    val bodyWaterRate: Double,  // percentage (0-100)
    val boneMass: Double,       // kg
    val metabolism: Int,        // kcal
    val muscleRate: Double,     // percentage (0-100)
    val visceralFat: Double     // level
) 