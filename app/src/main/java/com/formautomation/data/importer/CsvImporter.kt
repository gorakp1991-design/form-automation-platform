package com.formautomation.data.importer

import com.formautomation.domain.model.Dataset
import com.formautomation.domain.model.DataRow
import java.io.File

class CsvImporter {
    fun import(file: File): ImportResult {
        return try {
            val rows = mutableListOf<DataRow>()
            val headers = mutableListOf<String>()
            var rowIndex = 0
            val headerMap = mutableMapOf<Int, String>()

            file.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    if (rowIndex == 0) {
                        // Parse header row
                        val headerValues = parseCsvLine(line)
                        headers.addAll(headerValues)
                        headerValues.forEachIndexed { index, header ->
                            headerMap[index] = header
                        }
                    } else {
                        // Parse data rows
                        val values = parseCsvLine(line)
                        if (values.isNotEmpty() && values.size == headers.size) {
                            val rowMap = mutableMapOf<String, String>()
                            values.forEachIndexed { index, value ->
                                headerMap[index]?.let {
                                    rowMap[it] = value
                                }
                            }
                            rows.add(
                                DataRow(
                                    id = rowIndex,
                                    values = rowMap,
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
            ImportResult.Error(e.message ?: "Unknown error during CSV import")
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val values = mutableListOf<String>()
        var currentValue = StringBuilder()
        var insideQuotes = false
        var i = 0

        while (i < line.length) {
            val char = line[i]

            when {
                char == '"' -> {
                    insideQuotes = !insideQuotes
                }
                char == ',' && !insideQuotes -> {
                    values.add(currentValue.toString().trim())
                    currentValue = StringBuilder()
                }
                else -> {
                    currentValue.append(char)
                }
            }
            i++
        }

        values.add(currentValue.toString().trim())
        return values.filter { it.isNotEmpty() }
    }

    fun detectDuplicates(rows: List<DataRow>): Int {
        val seen = mutableSetOf<String>()
        var duplicateCount = 0

        rows.forEach { row ->
            val rowHash = row.values.values.joinToString("|").hashCode().toString()
            if (seen.contains(rowHash)) {
                duplicateCount++
            } else {
                seen.add(rowHash)
            }
        }

        return duplicateCount
    }
}

sealed class ImportResult {
    data class Success(val dataset: Dataset) : ImportResult()
    data class Error(val message: String) : ImportResult()
}
