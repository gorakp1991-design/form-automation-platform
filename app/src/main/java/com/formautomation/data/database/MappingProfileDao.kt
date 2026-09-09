package com.formautomation.data.database

import androidx.room.*

@Dao
interface MappingProfileDao {
    @Insert
    suspend fun insertProfile(profile: MappingProfileEntity): Long

    @Insert
    suspend fun insertMappings(mappings: List<FieldMappingEntity>)

    @Query("SELECT * FROM mapping_profiles WHERE id = :id")
    suspend fun getProfile(id: Long): MappingProfileEntity?

    @Query("SELECT * FROM mapping_profiles")
    suspend fun getAllProfiles(): List<MappingProfileEntity>

    @Query("SELECT * FROM field_mappings WHERE profileId = :profileId ORDER BY mappingOrder")
    suspend fun getMappings(profileId: Long): List<FieldMappingEntity>

    @Update
    suspend fun updateProfile(profile: MappingProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: MappingProfileEntity)

    @Query("DELETE FROM field_mappings WHERE profileId = :profileId")
    suspend fun deleteMappingsForProfile(profileId: Long)
}
