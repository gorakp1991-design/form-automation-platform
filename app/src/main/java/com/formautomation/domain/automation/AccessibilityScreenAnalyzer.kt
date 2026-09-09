package com.formautomation.domain.automation

import com.formautomation.data.accessibility.AccessibilityActionExecutor
import com.formautomation.data.accessibility.FieldDetectionResult
import com.formautomation.data.accessibility.FieldDetector
import com.formautomation.domain.model.FieldMapping
import com.formautomation.service.AccessibilityNodeData
import kotlinx.coroutines.delay

data class AccessibilityScreenState(
    val isAccessibilityEnabled: Boolean = false,
    val detectedFields: List<FieldDetectionResult> = emptyList(),
    val saveButtonDetected: Boolean = false,
    val nextButtonDetected: Boolean = false,
    val errorMessage: String? = null,
    val lastCheckTime: Long = System.currentTimeMillis()
)

class AccessibilityScreenAnalyzer(
    private val fieldDetector: FieldDetector,
    private val actionExecutor: AccessibilityActionExecutor
) {
    private var screenState = AccessibilityScreenState()

    suspend fun analyzeCurrentScreen(
        mappings: List<FieldMapping>
    ): Result<AccessibilityScreenState> {
        return try {
            screenState = screenState.copy(
                detectedFields = emptyList(),
                saveButtonDetected = false,
                nextButtonDetected = false,
                errorMessage = null
            )

            // Detect all fields
            val detectedFields = fieldDetector.detectAllFieldsOnScreen(mappings)
            screenState = screenState.copy(
                detectedFields = detectedFields,
                isAccessibilityEnabled = true
            )

            // Check for save button
            val saveButton = fieldDetector.detectSaveButton()
            if (saveButton != null) {
                screenState = screenState.copy(saveButtonDetected = true)
            }

            // Check for next button
            val nextButton = fieldDetector.detectNextButton()
            if (nextButton != null) {
                screenState = screenState.copy(nextButtonDetected = true)
            }

            val allFieldsFound = detectedFields.all { it.found }
            if (!allFieldsFound) {
                val missingFields = detectedFields.filter { !it.found }
                screenState = screenState.copy(
                    errorMessage = "Missing fields: ${missingFields.map { it.fieldName }.joinToString(", ")}"
                )
            }

            Result.success(screenState)
        } catch (e: Exception) {
            screenState = screenState.copy(
                errorMessage = e.message ?: "Unknown error",
                isAccessibilityEnabled = false
            )
            Result.failure(e)
        }
    }

    suspend fun verifyFieldsForMapping(
        mappings: List<FieldMapping>
    ): Result<Map<String, Boolean>> {
        return try {
            val detectionResults = fieldDetector.detectAllFieldsOnScreen(mappings)
            val verificationMap = detectionResults.associate { result ->
                result.fieldName to result.found
            }
            Result.success(verificationMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scrollToFindField(
        fieldName: String,
        maxScrollAttempts: Int = 5
    ): Result<AccessibilityNodeData?> {
        return try {
            repeat(maxScrollAttempts) {
                val fieldData = fieldDetector.detectFieldOnScreen(
                    FieldMapping(
                        id = 0,
                        spreadsheetColumn = fieldName,
                        formFieldId = fieldName,
                        formFieldName = fieldName,
                        fieldType = "TEXT",
                        isRequired = true,
                        order = 0
                    )
                )

                if (fieldData.found) {
                    return Result.success(fieldData.nodeInfo)
                }

                // Scroll down and try again
                delay(500)
            }

            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun waitForScreenChange(
        timeout: Long = 5000
    ): Result<Unit> {
        return try {
            val startTime = System.currentTimeMillis()
            val initialState = screenState.copy()

            while (System.currentTimeMillis() - startTime < timeout) {
                delay(500)
                // In real implementation, this would track actual screen changes
                if (screenState != initialState) {
                    return Result.success(Unit)
                }
            }

            Result.failure(Exception("Screen did not change within ${timeout}ms"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getScreenState(): AccessibilityScreenState {
        return screenState
    }

    fun getDetectionSummary(): String {
        val summary = StringBuilder()
        summary.append("=== ACCESSIBILITY SCREEN ANALYSIS ===\n")
        summary.append("Accessibility Enabled: ${screenState.isAccessibilityEnabled}\n")
        summary.append("Detected Fields: ${screenState.detectedFields.count { it.found }}/${screenState.detectedFields.size}\n")
        summary.append("Save Button Detected: ${screenState.saveButtonDetected}\n")
        summary.append("Next Button Detected: ${screenState.nextButtonDetected}\n")
        if (screenState.errorMessage != null) {
            summary.append("Errors: ${screenState.errorMessage}\n")
        }
        summary.append("\nDetailed Field Status:\n")
        screenState.detectedFields.forEach { result ->
            val status = if (result.found) "✓ FOUND" else "✗ NOT FOUND"
            summary.append("  $status - ${result.fieldName}")
            if (result.errorMessage != null) {
                summary.append(" (${result.errorMessage})")
            }
            summary.append("\n")
        }
        return summary.toString()
    }
}
