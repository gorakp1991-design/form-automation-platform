package com.formautomation.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mapping_profiles")
data class MappingProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val mappingJson: String, // { "column_name": "form_field_id" }
    val createdAt: Long,
    val updatedAt: Long
)