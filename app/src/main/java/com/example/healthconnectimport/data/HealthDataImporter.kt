package com.example.healthconnectimport.data

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.*
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Power
import androidx.health.connect.client.units.Length
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Percentage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class HealthDataImporter(private val healthConnectClient: HealthConnectClient) {
    
    suspend fun importBodyCompositionData(dataList: List<BodyCompositionData>): ImportResult {
        return withContext(Dispatchers.IO) {
            val records = mutableListOf<Record>()
            var successCount = 0
            var errorCount = 0
            val errors = mutableListOf<String>()
            
            for (data in dataList) {
                try {
                    val instant = data.time.toInstant()
                    
                    // Weight Record
                    records.add(
                        WeightRecord(
                            weight = Mass.kilograms(data.weight),
                            time = instant,
                            zoneOffset = data.time.offset
                        )
                    )
                    
                    // Height Record
                    records.add(
                        HeightRecord(
                            height = Length.meters(data.height / 100.0), // convert cm to meters
                            time = instant,
                            zoneOffset = data.time.offset
                        )
                    )
                    
                    // Body Fat Record
                    records.add(
                        BodyFatRecord(
                            percentage = Percentage(data.fatRate),
                            time = instant,
                            zoneOffset = data.time.offset
                        )
                    )
                    
                    // Bone Mass Record
                    records.add(
                        BoneMassRecord(
                            mass = Mass.kilograms(data.boneMass),
                            time = instant,
                            zoneOffset = data.time.offset
                        )
                    )
                    
                    // Basal Metabolic Rate Record
                    records.add(
                        BasalMetabolicRateRecord(
                            basalMetabolicRate = Power.kilocaloriesPerDay(data.metabolism.toDouble()),
                            time = instant,
                            zoneOffset = data.time.offset
                        )
                    )
                    
                    // Lean Body Mass Record (calculated from muscle rate and weight)
                    val leanBodyMass = data.weight * (data.muscleRate / 100.0)
                    records.add(
                        LeanBodyMassRecord(
                            mass = Mass.kilograms(leanBodyMass),
                            time = instant,
                            zoneOffset = data.time.offset
                        )
                    )
                    
                    successCount++
                } catch (e: Exception) {
                    errorCount++
                    errors.add("Error processing data at ${data.time}: ${e.message}")
                }
            }
            
            // Insert all records to Health Connect
            try {
                healthConnectClient.insertRecords(records)
                ImportResult(
                    totalRecords = dataList.size,
                    successfulRecords = successCount,
                    failedRecords = errorCount,
                    errors = errors,
                    isSuccess = true
                )
            } catch (e: Exception) {
                ImportResult(
                    totalRecords = dataList.size,
                    successfulRecords = 0,
                    failedRecords = dataList.size,
                    errors = listOf("Failed to insert records to Health Connect: ${e.message}"),
                    isSuccess = false
                )
            }
        }
    }
}

data class ImportResult(
    val totalRecords: Int,
    val successfulRecords: Int,
    val failedRecords: Int,
    val errors: List<String>,
    val isSuccess: Boolean
)  