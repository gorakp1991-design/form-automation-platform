package com.formautomation.domain.automation

data class ErrorAnalysis(
    val timestamp: Long = System.currentTimeMillis(),
    val fieldId: String = "",
    val fieldName: String = "",
    val expectedValue: String = "",
    val actualValue: String = "",
    val errorType: String = "", // INPUT_MISMATCH, FOCUS_FAILED, KEYBOARD_ERROR, SAVE_FAILED, TIMEOUT
    val errorMessage: String = "",
    val recoveryAttempts: Int = 0,
    val recoverySuccessful: Boolean = false,
    val stateSnapshot: Map<String, String> = emptyMap()
)

class ErrorRecoveryManager {
    private val errorHistory = mutableListOf<ErrorAnalysis>()
    private var currentError: ErrorAnalysis? = null

    fun recordError(
        fieldId: String,
        fieldName: String,
        expectedValue: String,
        actualValue: String,
        errorType: String,
        errorMessage: String,
        stateSnapshot: Map<String, String> = emptyMap()
    ) {
        currentError = ErrorAnalysis(
            fieldId = fieldId,
            fieldName = fieldName,
            expectedValue = expectedValue,
            actualValue = actualValue,
            errorType = errorType,
            errorMessage = errorMessage,
            stateSnapshot = stateSnapshot
        )
        errorHistory.add(currentError!!)
    }

    fun recordRecoveryAttempt(successful: Boolean, attemptNumber: Int) {
        currentError?.let {
            val updated = it.copy(
                recoveryAttempts = attemptNumber,
                recoverySuccessful = successful
            )
            currentError = updated
            val index = errorHistory.lastIndex
            if (index >= 0) {
                errorHistory[index] = updated
            }
        }
    }

    fun getErrorHistory(): List<ErrorAnalysis> {
        return errorHistory.toList()
    }

    fun getCurrentError(): ErrorAnalysis? {
        return currentError
    }

    fun getErrorsByType(type: String): List<ErrorAnalysis> {
        return errorHistory.filter { it.errorType == type }
    }

    fun getErrorSummary(): Map<String, Int> {
        return errorHistory.groupingBy { it.errorType }.eachCount()
    }

    fun clearErrorHistory() {
        errorHistory.clear()
        currentError = null
    }

    fun getDetailedErrorReport(): String {
        val summary = StringBuilder()
        summary.append("=== ERROR ANALYSIS REPORT ===\n")
        summary.append("Total Errors: ${errorHistory.size}\n")
        summary.append("Timestamp: ${System.currentTimeMillis()}\n\n")

        summary.append("Error Summary by Type:\n")
        getErrorSummary().forEach { (type, count) ->
            summary.append("  - $type: $count\n")
        }

        summary.append("\nDetailed Error Log:\n")
        errorHistory.forEachIndexed { index, error ->
            summary.append("\n[Error #${index + 1}]\n")
            summary.append("  Field: ${error.fieldName} (${error.fieldId})\n")
            summary.append("  Type: ${error.errorType}\n")
            summary.append("  Expected: ${error.expectedValue}\n")
            summary.append("  Actual: ${error.actualValue}\n")
            summary.append("  Message: ${error.errorMessage}\n")
            summary.append("  Recovery Attempts: ${error.recoveryAttempts}\n")
            summary.append("  Recovery Successful: ${error.recoverySuccessful}\n")
            summary.append("  Time: ${error.timestamp}\n")
        }

        return summary.toString()
    }
}
