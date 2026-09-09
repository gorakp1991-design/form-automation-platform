package com.formautomation.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "field_mappings")
data class FieldMappingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val spreadsheetColumn: String,
    val formFieldId: String,
    val formFieldName: String,
    val fieldType: String,
    val isRequired: Boolean,
    val validationJson: String?, // JSON serialized ValidationRule
    val mappingOrder: Int
)

@Entity(tableName = "mapping_profiles")
data class MappingProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val datasetId: Long,
    val successIndicatorJson: String?, // JSON serialized SuccessIndicator
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = false
)
