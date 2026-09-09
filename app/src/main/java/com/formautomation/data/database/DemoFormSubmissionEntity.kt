package com.formautomation.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "demo_form_submissions")
data class DemoFormSubmissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "submission_number")
    val submissionNumber: Int,
    @ColumnInfo(name = "form_data_json")
    val formDataJson: String, // JSON of submitted form data
    @ColumnInfo(name = "status")
    val status: String, // SUCCESS, FAILED, PENDING
    @ColumnInfo(name = "submitted_at")
    val submittedAt: Long,
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null
)
