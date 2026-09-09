package com.formautomation.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DatasetEntity::class,
        DataRowEntity::class,
        MappingProfileEntity::class,
        AutomationSessionEntity::class,
        SessionProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun datasetDao(): DatasetDao
    abstract fun mappingProfileDao(): MappingProfileDao
    abstract fun automationSessionDao(): AutomationSessionDao
}

interface DatasetDao {
    suspend fun insertDataset(dataset: DatasetEntity)
    suspend fun getDataset(id: Long): DatasetEntity?
    suspend fun getAllDatasets(): List<DatasetEntity>
    suspend fun getDatasetRows(datasetId: Long): List<DataRowEntity>
    suspend fun updateRowStatus(rowId: Long, status: String)
}

interface MappingProfileDao {
    suspend fun insertProfile(profile: MappingProfileEntity)
    suspend fun getProfile(id: Long): MappingProfileEntity?
    suspend fun getAllProfiles(): List<MappingProfileEntity>
}

interface AutomationSessionDao {
    suspend fun insertSession(session: AutomationSessionEntity)
    suspend fun getSession(id: Long): AutomationSessionEntity?
    suspend fun updateSessionProgress(sessionId: Long, progress: SessionProgressEntity)
}