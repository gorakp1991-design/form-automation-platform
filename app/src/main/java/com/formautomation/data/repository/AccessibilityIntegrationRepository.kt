package com.formautomation.data.repository

import com.formautomation.data.accessibility.AccessibilityActionExecutor
import com.formautomation.data.accessibility.FieldDetector
import com.formautomation.domain.automation.AccessibilityScreenAnalyzer
import com.formautomation.domain.model.FieldMapping
import com.formautomation.domain.model.DataRow
import com.formautomation.service.AccessibilityNodeData

class AccessibilityIntegrationRepository(
    private val fieldDetector: FieldDetector,
    private val actionExecutor: AccessibilityActionExecutor,
    private val screenAnalyzer: AccessibilityScreenAnalyzer
) {
    suspend fun detectFormFields(
        mappings: List<FieldMapping>
    ): Result<List<Pair<FieldMapping, AccessibilityNodeData?>>> {
        return try {
            val results = fieldDetector.detectAllFieldsOnScreen(mappings)
            val mappedResults = results.map { result ->
                val mapping = mappings.find { it.formFieldId == result.fieldId }
                mapping?.let { it to result.nodeInfo }
            }.filterNotNull()
            Result.success(mappedResults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fillFormField(
        nodeData: AccessibilityNodeData,
        value: String
    ): Result<Unit> {
        return try {
            val result = actionExecutor.focusAndTypeIntoField(nodeData, value)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Field fill failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clickSaveButton(): Result<Unit> {
        return try {
            val saveButton = fieldDetector.detectSaveButton()
                ?: return Result.failure(Exception("Save button not found"))
            val result = actionExecutor.clickButton(saveButton)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Save click failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyFormSubmissionSuccess(
        successIndicators: List<String>
    ): Result<Boolean> {
        return try {
            val success = fieldDetector.detectFormSubmissionSuccess(successIndicators)
            Result.success(success)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzeScreen(
        mappings: List<FieldMapping>
    ): Result<String> {
        return try {
            val analysisResult = screenAnalyzer.analyzeCurrentScreen(mappings)
            if (analysisResult.isSuccess) {
                val summary = screenAnalyzer.getDetectionSummary()
                Result.success(summary)
            } else {
                Result.failure(analysisResult.exceptionOrNull() ?: Exception("Analysis failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scrollToField(
        fieldName: String
    ): Result<AccessibilityNodeData?> {
        return try {
            val result = screenAnalyzer.scrollToFindField(fieldName)
            if (result.isSuccess) {
                Result.success(result.getOrNull())
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Scroll failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
