package com.formautomation.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        DatasetEntity::class,
        DataRowEntity::class,
        MappingProfileEntity::class,
        FieldMappingEntity::class,
        AutomationSessionEntity::class,
        SessionProgressEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun datasetDao(): DatasetDao
    abstract fun mappingProfileDao(): MappingProfileDao
    abstract fun automationSessionDao(): AutomationSessionDao
}
