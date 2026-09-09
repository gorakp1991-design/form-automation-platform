package com.formautomation.data.repository

import com.formautomation.data.database.DemoFormSubmissionDao
import com.formautomation.data.database.DemoFormSubmissionEntity
import com.google.gson.Gson

class DemoFormRepository(
    private val demoFormSubmissionDao: DemoFormSubmissionDao,
    private val gson: Gson
) {
    suspend fun submitForm(
        submissionNumber: Int,
        formData: Map<String, String>
    ): Result<Unit> {
        return try {
            val submission = DemoFormSubmissionEntity(
                submissionNumber = submissionNumber,
                formDataJson = gson.toJson(formData),
                status = "SUCCESS",
                submittedAt = System.currentTimeMillis()
            )
            demoFormSubmissionDao.insertSubmission(submission)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordFailedSubmission(
        submissionNumber: Int,
        formData: Map<String, String>,
        errorMessage: String
    ): Result<Unit> {
        return try {
            val submission = DemoFormSubmissionEntity(
                submissionNumber = submissionNumber,
                formDataJson = gson.toJson(formData),
                status = "FAILED",
                submittedAt = System.currentTimeMillis(),
                errorMessage = errorMessage
            )
            demoFormSubmissionDao.insertSubmission(submission)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSuccessCount(): Int {
        return try {
            demoFormSubmissionDao.getSuccessfulCount()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getFailedCount(): Int {
        return try {
            demoFormSubmissionDao.getFailedCount()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getAllSubmissions(): List<Map<String, String>> {
        return try {
            demoFormSubmissionDao.getAllSubmissions().map { entity ->
                try {
                    gson.fromJson(entity.formDataJson, Map::class.java) as Map<String, String>
                } catch (e: Exception) {
                    emptyMap()
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun clearAllSubmissions(): Result<Unit> {
        return try {
            demoFormSubmissionDao.clearAllSubmissions()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
