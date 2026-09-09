package com.formautomation.domain.automation

import kotlinx.coroutines.delay

data class FieldFocusState(
    val fieldId: String = "",
    val fieldName: String = "",
    val isFocused: Boolean = false,
    val focusTime: Long = System.currentTimeMillis(),
    val focusAttempts: Int = 0
)

data class FieldInputProgress(
    val fieldId: String,
    val expectedValue: String,
    val currentInput: String = "",
    val currentPosition: Int = 0,
    val status: String, // PENDING, INPUTTING, COMPLETED, FAILED
    val error: String? = null
)

interface FieldFocusListener {
    suspend fun onFieldFocused(field: FieldFocusState)
    suspend fun onFieldInputStarted(field: FieldFocusState)
    suspend fun onFieldInputCompleted(progress: FieldInputProgress)
    suspend fun onFieldFocusLost()
}

class FieldFocusManager(
    private val focusListener: FieldFocusListener? = null
) {
    private var currentFocus: FieldFocusState = FieldFocusState()
    private var inputProgress: MutableMap<String, FieldInputProgress> = mutableMapOf()

    suspend fun focusField(
        fieldId: String,
        fieldName: String,
        maxRetries: Int = 3
    ): Result<FieldFocusState> {
        return try {
            var attemptCount = 0

            while (attemptCount < maxRetries) {
                try {
                    // Simulate field focus
                    delay(300)

                    currentFocus = FieldFocusState(
                        fieldId = fieldId,
                        fieldName = fieldName,
                        isFocused = true,
                        focusTime = System.currentTimeMillis(),
                        focusAttempts = attemptCount + 1
                    )

                    focusListener?.onFieldFocused(currentFocus)
                    return Result.success(currentFocus)
                } catch (e: Exception) {
                    attemptCount++
                    if (attemptCount >= maxRetries) {
                        return Result.failure(
                            Exception("Failed to focus field '$fieldName' after $maxRetries attempts")
                        )
                    }
                    delay(500)
                }
            }

            Result.failure(Exception("Unknown error focusing field"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startInputting(
        fieldId: String,
        expectedValue: String
    ): Result<FieldInputProgress> {
        return try {
            val progress = FieldInputProgress(
                fieldId = fieldId,
                expectedValue = expectedValue,
                currentInput = "",
                currentPosition = 0,
                status = "INPUTTING"
            )

            inputProgress[fieldId] = progress
            focusListener?.onFieldInputStarted(currentFocus)
            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInputProgress(
        fieldId: String,
        newInput: String,
        position: Int
    ): Result<FieldInputProgress> {
        return try {
            val currentProgress = inputProgress[fieldId] ?: return Result.failure(
                Exception("No input progress for field '$fieldId'")
            )

            val updatedProgress = currentProgress.copy(
                currentInput = newInput,
                currentPosition = position
            )

            inputProgress[fieldId] = updatedProgress
            Result.success(updatedProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeInput(fieldId: String): Result<FieldInputProgress> {
        return try {
            val progress = inputProgress[fieldId] ?: return Result.failure(
                Exception("No input progress for field '$fieldId'")
            )

            val isCorrect = progress.currentInput == progress.expectedValue
            val completedProgress = progress.copy(
                status = if (isCorrect) "COMPLETED" else "FAILED",
                error = if (!isCorrect) "Input mismatch: expected '${progress.expectedValue}', got '${progress.currentInput}'" else null
            )

            inputProgress[fieldId] = completedProgress
            focusListener?.onFieldInputCompleted(completedProgress)
            return Result.success(completedProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loseFocus(): Result<Unit> {
        return try {
            currentFocus = currentFocus.copy(isFocused = false)
            focusListener?.onFieldFocusLost()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentFocus(): FieldFocusState {
        return currentFocus
    }

    fun getInputProgress(fieldId: String): FieldInputProgress? {
        return inputProgress[fieldId]
    }

    fun getAllInputProgress(): Map<String, FieldInputProgress> {
        return inputProgress.toMap()
    }

    fun clearProgress() {
        inputProgress.clear()
        currentFocus = FieldFocusState()
    }
}
