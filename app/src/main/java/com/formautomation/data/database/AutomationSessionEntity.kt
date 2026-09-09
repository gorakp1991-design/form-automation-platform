package com.formautomation.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_sessions")
data class AutomationSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val datasetId: Long,
    val mappingProfileId: Long,
    val status: String, // IDLE, RUNNING, PAUSED, COMPLETED, STOPPED
    val startedAt: Long,
    val pausedAt: Long? = null,
    val resumedAt: Long? = null,
    val completedAt: Long? = null
)

@Entity(tableName = "session_progress")
data class SessionProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val totalRecords: Int,
    val completedRecords: Int,
    val failedRecords: Int,
    val skippedRecords: Int,
    val currentRecordIndex: Int,
    val lastSuccessfulRecordIndex: Int
)