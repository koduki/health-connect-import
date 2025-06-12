package com.example.healthconnectimport.data

import java.io.InputStream
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class CsvParser {
    fun parseBodyCompositionCsv(inputStream: InputStream): List<BodyCompositionData> {
        val result = mutableListOf<BodyCompositionData>()
        
        inputStream.bufferedReader().use { reader ->
            val lines = reader.readLines()
            
            if (lines.isEmpty()) return emptyList()
            
            // Skip header line
            val dataLines = lines.drop(1)
            
            for (line in dataLines) {
                if (line.isBlank()) continue
                
                try {
                    val columns = line.split(',')
                    
                    if (columns.size >= 10) {
                        // Skip line if essential data (time, weight, height) is null
                        if (isNullOrEmpty(columns[0]) || isNullOrEmpty(columns[1]) || isNullOrEmpty(columns[2])) {
                            continue
                        }
                        
                        val bodyComposition = BodyCompositionData(
                            time = parseDateTime(columns[0].trim()),
                            weight = parseDoubleOrDefault(columns[1], 0.0),
                            height = parseDoubleOrDefault(columns[2], 0.0),
                            bmi = parseDoubleOrDefault(columns[3], 0.0),
                            fatRate = parseDoubleOrDefault(columns[4], 0.0),
                            bodyWaterRate = parseDoubleOrDefault(columns[5], 0.0),
                            boneMass = parseDoubleOrDefault(columns[6], 0.0),
                            metabolism = parseDoubleOrDefault(columns[7], 0.0).toInt(),
                            muscleRate = parseDoubleOrDefault(columns[8], 0.0),
                            visceralFat = parseDoubleOrDefault(columns[9], 0.0)
                        )
                        result.add(bodyComposition)
                    }
                } catch (e: Exception) {
                    // Skip invalid lines
                    println("Error parsing line: $line - ${e.message}")
                }
            }
        }
        
        return result
    }
    
    private fun isNullOrEmpty(value: String): Boolean {
        return value.trim().isEmpty() || value.trim().equals("null", ignoreCase = true)
    }
    
    private fun parseDoubleOrDefault(value: String, default: Double): Double {
        return try {
            if (isNullOrEmpty(value)) default else value.trim().toDouble()
        } catch (e: Exception) {
            default
        }
    }
    
    private fun parseIntOrDefault(value: String, default: Int): Int {
        return try {
            if (isNullOrEmpty(value)) default else value.trim().toInt()
        } catch (e: Exception) {
            default
        }
    }
    
    private fun parseDateTime(timeString: String): ZonedDateTime {
        // Handle format: "2024-04-29 12:12:32+0000"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssZ")
        return ZonedDateTime.parse(timeString, formatter)
    }
} 