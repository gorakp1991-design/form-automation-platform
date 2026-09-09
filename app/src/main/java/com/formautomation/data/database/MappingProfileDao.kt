package com.formautomation.data.database

import androidx.room.*

@Dao
interface MappingProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: MappingProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMappings(mappings: List<FieldMappingEntity>)

    @Query("SELECT * FROM mapping_profiles WHERE id = :id")
    suspend fun getProfile(id: Long): MappingProfileEntity?

    @Query("SELECT * FROM mapping_profiles ORDER BY updated_at DESC")
    suspend fun getAllProfiles(): List<MappingProfileEntity>

    @Query("SELECT * FROM field_mappings WHERE profileId = :profileId ORDER BY mappingOrder ASC")
    suspend fun getMappings(profileId: Long): List<FieldMappingEntity>

    @Update
    suspend fun updateProfile(profile: MappingProfileEntity)

    @Query("DELETE FROM mapping_profiles WHERE id = :id")
    suspend fun deleteProfile(id: Long)

    @Query("DELETE FROM field_mappings WHERE profileId = :profileId")
    suspend fun deleteMappingsForProfile(profileId: Long)

    @Query("UPDATE mapping_profiles SET isActive = 0 WHERE id != :profileId")
    suspend fun deactivateOtherProfiles(profileId: Long)

    @Query("UPDATE mapping_profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun activateProfile(profileId: Long)
}
