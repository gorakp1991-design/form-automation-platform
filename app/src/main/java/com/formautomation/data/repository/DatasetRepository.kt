package com.formautomation.data.repository

import com.formautomation.data.database.DatasetDao
import com.formautomation.data.database.DatasetEntity
import com.formautomation.data.database.DataRowEntity
import com.formautomation.data.importer.CsvImporter
import com.formautomation.data.importer.ImportResult
import com.formautomation.domain.model.Dataset
import com.formautomation.domain.model.DataRow
import com.google.gson.Gson
import java.io.File
import java.security.MessageDigest

class DatasetRepository(
    private val datasetDao: DatasetDao,
    private val csvImporter: CsvImporter,
    private val gson: Gson
) {
    suspend fun importCsv(file: File): Result<Dataset> {
        return try {
            val result = csvImporter.import(file)
            when (result) {
                is ImportResult.Success -> {
                    val dataset = result.dataset
                    val duplicateCount = csvImporter.detectDuplicates(dataset.rows)
                    val fileHash = calculateFileHash(file)

                    val entity = DatasetEntity(
                        id = dataset.id,
                        name = dataset.name,
                        columnsJson = gson.toJson(dataset.columns),
                        totalRows = dataset.totalRows,
                        importedAt = dataset.importedAt,
                        fileHash = fileHash,
                        duplicateRowCount = duplicateCount
                    )
                    datasetDao.insertDataset(entity)

                    // Insert rows
                    val rowEntities = dataset.rows.map { row ->
                        DataRowEntity(
                            datasetId = dataset.id,
                            rowIndex = row.id,
                            valuesJson = gson.toJson(row.values),
                            status = row.status,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    }
                    datasetDao.insertRows(rowEntities)

                    Result.success(dataset)
                }
                is ImportResult.Error -> {
                    Result.failure(Exception(result.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDataset(id: Long): Dataset? {
        val entity = datasetDao.getDataset(id) ?: return null
        val rowEntities = datasetDao.getDatasetRows(id)

        val columns: List<String> = try {
            gson.fromJson(entity.columnsJson, List::class.java) as List<String>
        } catch (e: Exception) {
            emptyList()
        }

        val rows = rowEntities.map { rowEntity ->
            val values: Map<String, String> = try {
                gson.fromJson(rowEntity.valuesJson, Map::class.java) as Map<String, String>
            } catch (e: Exception) {
                emptyMap()
            }
            DataRow(
                id = rowEntity.rowIndex,
                values = values.toMutableMap(),
                status = rowEntity.status
            )
        }

        return Dataset(
            id = entity.id,
            name = entity.name,
            columns = columns,
            totalRows = entity.totalRows,
            rows = rows,
            importedAt = entity.importedAt,
            fileHash = entity.fileHash,
            duplicateRowCount = entity.duplicateRowCount
        )
    }

    suspend fun getAllDatasets(): List<Dataset> {
        return datasetDao.getAllDatasets().mapNotNull { entity ->
            getDataset(entity.id)
        }
    }

    suspend fun deleteDataset(id: Long) {
        datasetDao.deleteDatasetRows(id)
        datasetDao.deleteDataset(id)
    }

    private fun calculateFileHash(file: File): String {
        return try {
            file.inputStream().use { stream ->
                MessageDigest.getInstance("SHA-256").let { digest ->
                    stream.readBytes().forEach { digest.update(it) }
                    digest.digest().joinToString("") { "%02x".format(it) }
                }
            }
        } catch (e: Exception) {
            ""
        }
    }
}
