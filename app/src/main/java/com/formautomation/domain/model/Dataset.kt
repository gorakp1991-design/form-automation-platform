package com.formautomation.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "datasets")
data class Dataset(
    @PrimaryKey
    val id: Long,
    val name: String,
    val columns: List<String>,
    val totalRows: Int,
    val rows: List<DataRow>,
    val importedAt: Long,
    val fileHash: String = "",
    val duplicateRowCount: Int = 0
)

data class DataRow(
    val id: Int,
    val values: MutableMap<String, String>,
    val status: String // PENDING, RUNNING, COMPLETED, FAILED, SKIPPED
)