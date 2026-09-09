package com.formautomation.domain.automation

import com.formautomation.domain.model.FieldMapping
import com.formautomation.domain.model.DataRow
import kotlinx.coroutines.delay

enum class AutomationStateEnum {
    IDLE,
    INITIALIZING,
    WAITING_FOR_FORM,
    PROCESSING_RECORD,
    FOCUSING_FIELD,
    INPUTTING_FIELD,
    VERIFYING_FIELD,
    MOVING_TO_NEXT_FIELD,
    READY_TO_SAVE,
    SAVING_FORM,
    WAITING_FOR_SUCCESS,
    LOADING_NEXT_FORM,
    LOADING_NEXT_RECORD,
    PAUSED,
    ERROR,
    COMPLETED,
    STOPPED
}

data class AutomationSessionState(
    val state: AutomationStateEnum = AutomationStateEnum.IDLE,
    val currentRecord: Int = 0,
    val totalRecords: Int = 0,
    val currentField: Int = 0,
    val totalFields: Int = 0,
    val completedRecords: Int = 0,
    val failedRecords: Int = 0,
    val error: String? = null,
    val lastStateChangeTime: Long = System.currentTimeMillis(),
    val pauseReason: String? = null
)

class AutomationEngine(
    private val inputController: InputController,
    private val formSaveController: FormSaveController,
    private val keyboardManager: KeyboardManager,
    private val fieldFocusManager: FieldFocusManager,
    private val navigationManager: FormNavigationManager,
    private val errorRecoveryManager: ErrorRecoveryManager
) {
    private var sessionState = AutomationSessionState()
    private val stateHistory = mutableListOf<AutomationSessionState>()

    suspend fun processRecord(
        recordIndex: Int,
        totalRecords: Int,
        row: DataRow,
        mappings: List<FieldMapping>,
        onProgress: suspend (AutomationSessionState) -> Unit
    ): Result<AutomationSessionState> {
        return try {
            sessionState = sessionState.copy(
                state = AutomationStateEnum.PROCESSING_RECORD,
                currentRecord = recordIndex,
                totalRecords = totalRecords,
                totalFields = mappings.size
            )
            stateHistory.add(sessionState)
            onProgress(sessionState)

            // Process each field
            mappings.forEachIndexed { fieldIndex, mapping ->
                sessionState = sessionState.copy(
                    currentField = fieldIndex,
                    state = AutomationStateEnum.FOCUSING_FIELD
                )
                stateHistory.add(sessionState)
                onProgress(sessionState)

                val value = row.values[mapping.spreadsheetColumn] ?: ""

                // Input the field value
                val inputResult = inputController.inputFieldValue(
                    fieldId = mapping.formFieldId,
                    fieldName = mapping.formFieldName,
                    value = value
                )

                if (inputResult.isFailure) {
                    sessionState = sessionState.copy(
                        state = AutomationStateEnum.ERROR,
                        error = inputResult.exceptionOrNull()?.message ?: "Input failed"
                    )
                    stateHistory.add(sessionState)
                    onProgress(sessionState)

                    errorRecoveryManager.recordError(
                        fieldId = mapping.formFieldId,
                        fieldName = mapping.formFieldName,
                        expectedValue = value,
                        actualValue = "",
                        errorType = "INPUT_FAILED",
                        errorMessage = inputResult.exceptionOrNull()?.message ?: "Unknown error"
                    )
                    return Result.failure(inputResult.exceptionOrNull() ?: Exception("Input failed"))
                }

                sessionState = sessionState.copy(
                    state = AutomationStateEnum.VERIFYING_FIELD,
                    currentField = fieldIndex + 1
                )
                stateHistory.add(sessionState)
                onProgress(sessionState)

                delay(300) // Small delay between fields
            }

            // All fields complete, save the form
            sessionState = sessionState.copy(
                state = AutomationStateEnum.READY_TO_SAVE
            )
            stateHistory.add(sessionState)
            onProgress(sessionState)

            val saveResult = formSaveController.clickSaveButton()
            if (saveResult.isFailure) {
                sessionState = sessionState.copy(
                    state = AutomationStateEnum.ERROR,
                    error = "Failed to click save button"
                )
                stateHistory.add(sessionState)
                onProgress(sessionState)
                return Result.failure(saveResult.exceptionOrNull() ?: Exception("Save failed"))
            }

            sessionState = sessionState.copy(state = AutomationStateEnum.WAITING_FOR_SUCCESS)
            stateHistory.add(sessionState)
            onProgress(sessionState)

            val successResult = formSaveController.waitForSuccessIndicator()
            if (successResult.isFailure) {
                sessionState = sessionState.copy(
                    state = AutomationStateEnum.ERROR,
                    error = "Form save failed",
                    failedRecords = sessionState.failedRecords + 1
                )
                stateHistory.add(sessionState)
                onProgress(sessionState)
                return Result.failure(successResult.exceptionOrNull() ?: Exception("Success detection failed"))
            }

            // Record completed successfully
            sessionState = sessionState.copy(
                state = AutomationStateEnum.LOADING_NEXT_FORM,
                completedRecords = sessionState.completedRecords + 1
            )
            stateHistory.add(sessionState)
            onProgress(sessionState)

            navigationManager.markRecordComplete(recordIndex)

            // Load next form if there are more records
            if (recordIndex < totalRecords - 1) {
                val nextResult = navigationManager.moveToNextRecord(totalRecords)
                if (nextResult.isFailure) {
                    sessionState = sessionState.copy(
                        state = AutomationStateEnum.ERROR,
                        error = "Failed to move to next record"
                    )
                    stateHistory.add(sessionState)
                    return Result.failure(nextResult.exceptionOrNull() ?: Exception("Navigation failed"))
                }

                val loadResult = navigationManager.loadNextForm()
                if (loadResult.isFailure) {
                    sessionState = sessionState.copy(
                        state = AutomationStateEnum.ERROR,
                        error = "Failed to load next form"
                    )
                    stateHistory.add(sessionState)
                    return Result.failure(loadResult.exceptionOrNull() ?: Exception("Form load failed"))
                }
            }

            Result.success(sessionState)
        } catch (e: Exception) {
            sessionState = sessionState.copy(
                state = AutomationStateEnum.ERROR,
                error = e.message ?: "Unknown error"
            )
            stateHistory.add(sessionState)
            Result.failure(e)
        }
    }

    suspend fun pause(reason: String): Result<AutomationSessionState> {
        return try {
            sessionState = sessionState.copy(
                state = AutomationStateEnum.PAUSED,
                pauseReason = reason
            )
            stateHistory.add(sessionState)
            Result.success(sessionState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resume(): Result<AutomationSessionState> {
        return try {
            sessionState = sessionState.copy(
                state = AutomationStateEnum.PROCESSING_RECORD,
                pauseReason = null
            )
            stateHistory.add(sessionState)
            Result.success(sessionState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun stop(): Result<AutomationSessionState> {
        return try {
            sessionState = sessionState.copy(
                state = AutomationStateEnum.STOPPED
            )
            stateHistory.add(sessionState)
            Result.success(sessionState)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSessionState(): AutomationSessionState {
        return sessionState
    }

    fun getStateHistory(): List<AutomationSessionState> {
        return stateHistory.toList()
    }

    fun getErrorAnalysis(): String {
        return errorRecoveryManager.getDetailedErrorReport()
    }
}
