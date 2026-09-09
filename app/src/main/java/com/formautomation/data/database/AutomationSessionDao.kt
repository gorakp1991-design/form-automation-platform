package com.formautomation.data.database

import androidx.room.*

@Dao
interface AutomationSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AutomationSessionEntity): Long

    @Query("SELECT * FROM automation_sessions WHERE id = :id")
    suspend fun getSession(id: Long): AutomationSessionEntity?

    @Query("SELECT * FROM automation_sessions ORDER BY startedAt DESC")
    suspend fun getAllSessions(): List<AutomationSessionEntity>

    @Update
    suspend fun updateSession(session: AutomationSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: SessionProgressEntity)

    @Query("SELECT * FROM session_progress WHERE sessionId = :sessionId")
    suspend fun getProgress(sessionId: Long): SessionProgressEntity?

    @Query("UPDATE session_progress SET completedRecords = :completed, failedRecords = :failed, currentRecordIndex = :current, lastSuccessfulRecordIndex = :lastSuccessful WHERE sessionId = :sessionId")
    suspend fun updateProgress(
        sessionId: Long,
        completed: Int,
        failed: Int,
        current: Int,
        lastSuccessful: Int
    )

    @Query("DELETE FROM automation_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)
}
