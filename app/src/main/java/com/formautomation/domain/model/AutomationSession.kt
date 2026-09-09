package com.formautomation.domain.model

data class AutomationSession(
    val id: Long = 0,
    val datasetId: Long,
    val mappingProfileId: Long,
    val status: String, // IDLE, INITIALIZING, RUNNING, PAUSED, STOPPED, COMPLETED
    val startedAt: Long,
    val pausedAt: Long? = null,
    val resumedAt: Long? = null,
    val completedAt: Long? = null,
    val progress: SessionProgress? = null
)

data class SessionProgress(
    val totalRecords: Int,
    val completedRecords: Int,
    val failedRecords: Int,
    val skippedRecords: Int = 0,
    val currentRecordIndex: Int,
    val lastSuccessfulRecordIndex: Int
) {
    val remainingRecords: Int
        get() = totalRecords - completedRecords - failedRecords - skippedRecords

    val progressPercentage: Float
        get() = if (totalRecords == 0) 0f else ((completedRecords + failedRecords) / totalRecords.toFloat()) * 100
}

enum class AutomationState {
    IDLE,
    INITIALIZING,
    WAITING_FOR_FORM,
    FINDING_FIELD,
    FOCUSING_FIELD,
    INPUTTING,
    VERIFYING,
    NEXT_FIELD,
    READY_TO_SAVE,
    SAVING,
    WAITING_FOR_SUCCESS,
    LOADING_NEXT_FORM,
    NEXT_RECORD,
    PAUSED,
    ERROR,
    COMPLETED,
    STOPPED
}
