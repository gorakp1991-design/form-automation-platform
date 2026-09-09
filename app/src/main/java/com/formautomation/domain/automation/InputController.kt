package com.formautomation.domain.automation

import kotlinx.coroutines.delay

data class InputControllerState(
    val fieldId: String = "",
    val expectedValue: String = "",
    val currentInput: String = "",
    val lastInputPosition: Int = 0,
    val status: String = "IDLE", // IDLE, FOCUSING, TYPING, PAUSED, COMPLETED, ERROR
    val error: String? = null,
    val typingStartTime: Long = 0,
    val typingEndTime: Long = 0
)

class InputController(
    private val typingSimulator: TypingSimulator,
    private val keyboardManager: KeyboardManager,
    private val fieldFocusManager: FieldFocusManager
) {
    private var currentState = InputControllerState()
    private val stateHistory = mutableListOf<InputControllerState>()

    suspend fun inputFieldValue(
        fieldId: String,
        fieldName: String,
        value: String,
        delayBetweenChars: Long = 50
    ): Result<InputControllerState> {
        return try {
            currentState = InputControllerState(
                fieldId = fieldId,
                expectedValue = value,
                status = "FOCUSING",
                typingStartTime = System.currentTimeMillis()
            )
            stateHistory.add(currentState)

            // Step 1: Focus the field
            val focusResult = fieldFocusManager.focusField(fieldId, fieldName)
            if (focusResult.isFailure) {
                currentState = currentState.copy(
                    status = "ERROR",
                    error = focusResult.exceptionOrNull()?.message ?: "Unknown focus error"
                )
                stateHistory.add(currentState)
                return Result.failure(focusResult.exceptionOrNull() ?: Exception("Focus failed"))
            }

            // Step 2: Start input tracking
            fieldFocusManager.startInputting(fieldId, value)

            // Step 3: Show keyboard
            val keyboardResult = keyboardManager.showKeyboard()
            if (keyboardResult.isFailure) {
                currentState = currentState.copy(
                    status = "ERROR",
                    error = "Failed to show keyboard"
                )
                stateHistory.add(currentState)
                return Result.failure(keyboardResult.exceptionOrNull() ?: Exception("Keyboard error"))
            }

            currentState = currentState.copy(status = "TYPING")
            stateHistory.add(currentState)

            // Step 4: Type letter by letter
            val typingResult = typingSimulator.typeLetterByLetter(
                text = value,
                onKeyPress = { char ->
                    currentState = currentState.copy(
                        currentInput = currentState.currentInput + char,
                        lastInputPosition = currentState.currentInput.length + 1
                    )
                },
                delayBetweenChars = delayBetweenChars
            )

            if (typingResult.isFailure) {
                currentState = currentState.copy(
                    status = "ERROR",
                    error = typingResult.exceptionOrNull()?.message ?: "Typing error"
                )
                stateHistory.add(currentState)
                return Result.failure(typingResult.exceptionOrNull() ?: Exception("Typing failed"))
            }

            // Step 5: Verify input
            delay(300)
            val verifyResult = fieldFocusManager.completeInput(fieldId)
            if (verifyResult.isFailure) {
                currentState = currentState.copy(
                    status = "ERROR",
                    error = verifyResult.exceptionOrNull()?.message ?: "Verification failed"
                )
                stateHistory.add(currentState)
                return Result.failure(verifyResult.exceptionOrNull() ?: Exception("Verification failed"))
            }

            currentState = currentState.copy(
                status = "COMPLETED",
                typingEndTime = System.currentTimeMillis()
            )
            stateHistory.add(currentState)

            Result.success(currentState)
        } catch (e: Exception) {
            currentState = currentState.copy(
                status = "ERROR",
                error = e.message ?: "Unknown error"
            )
            stateHistory.add(currentState)
            Result.failure(e)
        }
    }

    suspend fun recoverFromKeyboardDisappearance(
        fieldId: String,
        resumeFromPosition: Int
    ): Result<InputControllerState> {
        return try {
            currentState = currentState.copy(status = "PAUSED")
            stateHistory.add(currentState)

            // Recover keyboard
            val keyboardRecovery = keyboardManager.recoverFromKeyboardDisappearance()
            if (keyboardRecovery.isFailure) {
                currentState = currentState.copy(
                    status = "ERROR",
                    error = "Failed to recover keyboard"
                )
                stateHistory.add(currentState)
                return Result.failure(keyboardRecovery.exceptionOrNull() ?: Exception("Keyboard recovery failed"))
            }

            currentState = currentState.copy(status = "TYPING")
            stateHistory.add(currentState)

            // Continue typing from where we left off
            val remainingText = currentState.expectedValue.substring(resumeFromPosition)
            val typingResult = typingSimulator.continueTypingFromPosition(
                text = currentState.expectedValue,
                startPosition = resumeFromPosition,
                onKeyPress = { char ->
                    currentState = currentState.copy(
                        currentInput = currentState.currentInput + char,
                        lastInputPosition = currentState.currentInput.length + 1
                    )
                }
            )

            if (typingResult.isFailure) {
                currentState = currentState.copy(
                    status = "ERROR",
                    error = "Failed to resume typing"
                )
                stateHistory.add(currentState)
                return Result.failure(typingResult.exceptionOrNull() ?: Exception("Resume typing failed"))
            }

            currentState = currentState.copy(status = "COMPLETED")
            stateHistory.add(currentState)

            Result.success(currentState)
        } catch (e: Exception) {
            currentState = currentState.copy(
                status = "ERROR",
                error = e.message ?: "Recovery error"
            )
            stateHistory.add(currentState)
            Result.failure(e)
        }
    }

    fun getCurrentState(): InputControllerState {
        return currentState
    }

    fun getStateHistory(): List<InputControllerState> {
        return stateHistory.toList()
    }

    fun getLastInputPosition(): Int {
        return currentState.lastInputPosition
    }

    fun clearState() {
        currentState = InputControllerState()
        stateHistory.clear()
    }
}
