package com.formautomation.data.repository

import com.formautomation.data.database.*
import com.formautomation.domain.model.AutomationSession
import com.formautomation.domain.model.SessionProgress

class AutomationSessionRepository(
    private val automationSessionDao: AutomationSessionDao
) {
    suspend fun createSession(
        datasetId: Long,
        mappingProfileId: Long
    ): Result<Long> {
        return try {
            val session = AutomationSessionEntity(
                datasetId = datasetId,
                mappingProfileId = mappingProfileId,
                status = "IDLE",
                startedAt = System.currentTimeMillis()
            )
            val sessionId = automationSessionDao.insertSession(session)

            // Initialize progress
            val progress = SessionProgressEntity(
                sessionId = sessionId,
                totalRecords = 0,
                completedRecords = 0,
                failedRecords = 0,
                skippedRecords = 0,
                currentRecordIndex = 0,
                lastSuccessfulRecordIndex = 0
            )
            automationSessionDao.insertProgress(progress)

            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSession(id: Long): AutomationSession? {
        val entity = automationSessionDao.getSession(id) ?: return null
        val progress = automationSessionDao.getProgress(id)

        return AutomationSession(
            id = entity.id,
            datasetId = entity.datasetId,
            mappingProfileId = entity.mappingProfileId,
            status = entity.status,
            startedAt = entity.startedAt,
            pausedAt = entity.pausedAt,
            resumedAt = entity.resumedAt,
            completedAt = entity.completedAt,
            progress = progress?.let {
                SessionProgress(
                    totalRecords = it.totalRecords,
                    completedRecords = it.completedRecords,
                    failedRecords = it.failedRecords,
                    skippedRecords = it.skippedRecords,
                    currentRecordIndex = it.currentRecordIndex,
                    lastSuccessfulRecordIndex = it.lastSuccessfulRecordIndex
                )
            }
        )
    }

    suspend fun updateSessionStatus(sessionId: Long, status: String): Result<Unit> {
        return try {
            val session = automationSessionDao.getSession(sessionId) ?: return Result.failure(Exception("Session not found"))
            val updatedSession = session.copy(
                status = status,
                pausedAt = if (status == "PAUSED") System.currentTimeMillis() else session.pausedAt,
                resumedAt = if (status == "RUNNING") System.currentTimeMillis() else session.resumedAt,
                completedAt = if (status == "COMPLETED") System.currentTimeMillis() else session.completedAt
            )
            automationSessionDao.updateSession(updatedSession)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProgress(
        sessionId: Long,
        completed: Int,
        failed: Int,
        current: Int,
        lastSuccessful: Int
    ): Result<Unit> {
        return try {
            automationSessionDao.updateProgress(sessionId, completed, failed, current, lastSuccessful)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllSessions(): List<AutomationSession> {
        return automationSessionDao.getAllSessions().mapNotNull { entity ->
            getSession(entity.id)
        }
    }

    suspend fun deleteSession(sessionId: Long): Result<Unit> {
        return try {
            automationSessionDao.deleteSession(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
