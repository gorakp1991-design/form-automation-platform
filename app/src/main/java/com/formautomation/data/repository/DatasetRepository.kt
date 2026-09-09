package com.formautomation.data.repository

import com.formautomation.data.database.DatasetDao
import com.formautomation.data.database.DatasetEntity
import com.formautomation.data.importer.CsvImporter
import com.formautomation.domain.model.Dataset
import java.io.File

class DatasetRepository(
    private val datasetDao: DatasetDao,
    private val csvImporter: CsvImporter
) {
    suspend fun importCsv(file: File): Result<Dataset> {
        return try {
            val result = csvImporter.import(file)
            when (result) {
                is com.formautomation.data.importer.ImportResult.Success -> {
                    val dataset = result.dataset
                    val entity = DatasetEntity(
                        id = dataset.id,
                        name = dataset.name,
                        columnsJson = dataset.columns.joinToString(","),
                        totalRows = dataset.totalRows,
                        importedAt = dataset.importedAt,
                        fileHash = calculateFileHash(file),
                        duplicateRowCount = dataset.duplicateRowCount
                    )
                    datasetDao.insertDataset(entity)
                    Result.success(dataset)
                }
                is com.formautomation.data.importer.ImportResult.Error -> {
                    Result.failure(Exception(result.message))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDataset(id: Long): Dataset? {
        val entity = datasetDao.getDataset(id)
        return entity?.let { convertToDataset(it) }
    }

    suspend fun getAllDatasets(): List<Dataset> {
        return datasetDao.getAllDatasets().map { convertToDataset(it) }
    }

    private fun convertToDataset(entity: DatasetEntity): Dataset {
        return Dataset(
            id = entity.id,
            name = entity.name,
            columns = entity.columnsJson.split(","),
            totalRows = entity.totalRows,
            rows = emptyList(),
            importedAt = entity.importedAt,
            fileHash = entity.fileHash,
            duplicateRowCount = entity.duplicateRowCount
        )
    }

    private fun calculateFileHash(file: File): String {
        return file.inputStream().use { stream ->
            java.security.MessageDigest.getInstance("SHA-256").let { digest ->
                stream.readBytes().forEach { digest.update(it) }
                digest.digest().joinToString("") { "%02x".format(it) }
            }
        }
    }
}