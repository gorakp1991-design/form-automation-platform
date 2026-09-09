package com.formautomation.data.repository

import com.formautomation.data.database.FieldMappingEntity
import com.formautomation.data.database.MappingProfileDao
import com.formautomation.data.database.MappingProfileEntity
import com.formautomation.domain.model.FieldMapping
import com.formautomation.domain.model.MappingProfile
import com.formautomation.domain.model.SuccessIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MappingRepository(
    private val mappingDao: MappingProfileDao,
    private val gson: Gson
) {
    suspend fun createProfile(
        name: String,
        description: String,
        datasetId: Long,
        mappings: List<FieldMapping>,
        successIndicator: SuccessIndicator?
    ): Result<MappingProfile> {
        return try {
            val profileEntity = MappingProfileEntity(
                name = name,
                description = description,
                datasetId = datasetId,
                successIndicatorJson = successIndicator?.let { gson.toJson(it) },
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val profileId = mappingDao.insertProfile(profileEntity)

            val mappingEntities = mappings.map { mapping ->
                FieldMappingEntity(
                    profileId = profileId,
                    spreadsheetColumn = mapping.spreadsheetColumn,
                    formFieldId = mapping.formFieldId,
                    formFieldName = mapping.formFieldName,
                    fieldType = mapping.fieldType,
                    isRequired = mapping.isRequired,
                    validationJson = mapping.validation?.let { gson.toJson(it) },
                    mappingOrder = mapping.order
                )
            }

            mappingDao.insertMappings(mappingEntities)

            val profile = MappingProfile(
                id = profileId,
                name = name,
                description = description,
                datasetId = datasetId,
                mappings = mappings,
                successIndicator = successIndicator,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(id: Long): MappingProfile? {
        val profileEntity = mappingDao.getProfile(id) ?: return null
        val mappingEntities = mappingDao.getMappings(id)

        val mappings = mappingEntities.map { entity ->
            val validation = entity.validationJson?.let {
                gson.fromJson(it, com.formautomation.domain.model.ValidationRule::class.java)
            }
            FieldMapping(
                id = entity.id,
                spreadsheetColumn = entity.spreadsheetColumn,
                formFieldId = entity.formFieldId,
                formFieldName = entity.formFieldName,
                fieldType = entity.fieldType,
                isRequired = entity.isRequired,
                validation = validation,
                order = entity.mappingOrder
            )
        }.sortedBy { it.order }

        val successIndicator = profileEntity.successIndicatorJson?.let {
            gson.fromJson(it, SuccessIndicator::class.java)
        }

        return MappingProfile(
            id = profileEntity.id,
            name = profileEntity.name,
            description = profileEntity.description,
            datasetId = profileEntity.datasetId,
            mappings = mappings,
            successIndicator = successIndicator,
            createdAt = profileEntity.createdAt,
            updatedAt = profileEntity.updatedAt,
            isActive = profileEntity.isActive
        )
    }

    suspend fun getAllProfiles(): List<MappingProfile> {
        val profiles = mappingDao.getAllProfiles()
        return profiles.map { profileEntity ->
            val mappingEntities = mappingDao.getMappings(profileEntity.id)
            val mappings = mappingEntities.map { entity ->
                val validation = entity.validationJson?.let {
                    gson.fromJson(it, com.formautomation.domain.model.ValidationRule::class.java)
                }
                FieldMapping(
                    id = entity.id,
                    spreadsheetColumn = entity.spreadsheetColumn,
                    formFieldId = entity.formFieldId,
                    formFieldName = entity.formFieldName,
                    fieldType = entity.fieldType,
                    isRequired = entity.isRequired,
                    validation = validation,
                    order = entity.mappingOrder
                )
            }.sortedBy { it.order }

            val successIndicator = profileEntity.successIndicatorJson?.let {
                gson.fromJson(it, SuccessIndicator::class.java)
            }

            MappingProfile(
                id = profileEntity.id,
                name = profileEntity.name,
                description = profileEntity.description,
                datasetId = profileEntity.datasetId,
                mappings = mappings,
                successIndicator = successIndicator,
                createdAt = profileEntity.createdAt,
                updatedAt = profileEntity.updatedAt,
                isActive = profileEntity.isActive
            )
        }
    }

    suspend fun updateProfile(profile: MappingProfile): Result<Unit> {
        return try {
            val profileEntity = MappingProfileEntity(
                id = profile.id,
                name = profile.name,
                description = profile.description,
                datasetId = profile.datasetId,
                successIndicatorJson = profile.successIndicator?.let { gson.toJson(it) },
                createdAt = profile.createdAt,
                updatedAt = System.currentTimeMillis(),
                isActive = profile.isActive
            )
            mappingDao.updateProfile(profileEntity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProfile(id: Long): Result<Unit> {
        return try {
            mappingDao.deleteProfile(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
