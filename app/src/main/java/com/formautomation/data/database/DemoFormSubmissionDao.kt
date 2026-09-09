package com.formautomation.data.database

import androidx.room.*

@Dao
interface DemoFormSubmissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: DemoFormSubmissionEntity)

    @Query("SELECT * FROM demo_form_submissions ORDER BY submitted_at DESC")
    suspend fun getAllSubmissions(): List<DemoFormSubmissionEntity>

    @Query("SELECT COUNT(*) FROM demo_form_submissions WHERE status = 'SUCCESS'")
    suspend fun getSuccessfulCount(): Int

    @Query("SELECT COUNT(*) FROM demo_form_submissions WHERE status = 'FAILED'")
    suspend fun getFailedCount(): Int

    @Query("DELETE FROM demo_form_submissions")
    suspend fun clearAllSubmissions()
}
