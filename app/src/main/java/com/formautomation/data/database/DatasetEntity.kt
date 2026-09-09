package com.formautomation.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "datasets")
data class DatasetEntity(
    @PrimaryKey
    val id: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "columns_json")
    val columnsJson: String, // JSON array of column names
    @ColumnInfo(name = "total_rows")
    val totalRows: Int,
    @ColumnInfo(name = "imported_at")
    val importedAt: Long,
    @ColumnInfo(name = "file_hash")
    val fileHash: String,
    @ColumnInfo(name = "duplicate_row_count")
    val duplicateRowCount: Int
)

@Entity(tableName = "data_rows")
data class DataRowEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    @ColumnInfo(name = "dataset_id")
    val datasetId: Long,
    @ColumnInfo(name = "row_index")
    val rowIndex: Int,
    @ColumnInfo(name = "values_json")
    val valuesJson: String, // JSON object of column->value
    @ColumnInfo(name = "status")
    val status: String, // PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
