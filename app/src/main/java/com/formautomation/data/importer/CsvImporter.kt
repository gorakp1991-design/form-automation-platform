package com.formautomation.data.importer

import com.formautomation.domain.model.Dataset
import com.formautomation.domain.model.DataRow
import java.io.BufferedReader
import java.io.File

class CsvImporter {
    fun import(file: File): ImportResult {
        return try {
            val rows = mutableListOf<DataRow>()
            val headers = mutableListOf<String>()
            var rowIndex = 0

            file.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    if (rowIndex == 0) {
                        headers.addAll(line.split(",").map { it.trim().replace("\"", "") })
                    } else {
                        val values = parseCsvLine(line)
                        if (values.isNotEmpty()) {
                            rows.add(
                                DataRow(
                                    id = rowIndex,
                                    values = values.toMutableMap(),
                                    status = "PENDING"
                                )
                            )
                        }
                    }
                    rowIndex++
                }
            }

            if (headers.isEmpty()) {
                ImportResult.Error("No headers found in CSV")
            } else {
                val dataset = Dataset(
                    id = System.currentTimeMillis(),
                    name = file.nameWithoutExtension,
                    columns = headers,
                    totalRows = rows.size,
                    rows = rows,
                    importedAt = System.currentTimeMillis()
                )
                ImportResult.Success(dataset)
            }
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Unknown error")
        }
    }

    private fun parseCsvLine(line: String): Map<String, String> {
        val values = mutableMapOf<String, String>()
        val parts = line.split(",")
        // This is simplified; real CSV parsing is more complex
        return values
    }
}

sealed class ImportResult {
    data class Success(val dataset: Dataset) : ImportResult()
    data class Error(val message: String) : ImportResult()
}