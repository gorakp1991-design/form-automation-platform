package com.formautomation.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "datasets")
data class DatasetEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val columnsJson: String, // JSON array of column names
    val totalRows: Int,
    val importedAt: Long,
    val fileHash: String,
    val duplicateRowCount: Int
)

@Entity(tableName = "data_rows")
data class DataRowEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val datasetId: Long,
    val rowIndex: Int,
    val valuesJson: String, // JSON object of column->value
    val status: String, // PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
    val createdAt: Long,
    val updatedAt: Long
)