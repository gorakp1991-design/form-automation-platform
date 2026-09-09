package com.formautomation.domain.model

data class FieldMapping(
    val id: Long = 0,
    val spreadsheetColumn: String,
    val formFieldId: String,
    val formFieldName: String,
    val fieldType: String, // TEXT, EMAIL, PHONE, DATE, NUMBER, TEXTAREA, SELECT
    val isRequired: Boolean,
    val validation: ValidationRule? = null,
    val order: Int
)

data class ValidationRule(
    val type: String, // REQUIRED, EMAIL, PHONE, DATE_FORMAT, LENGTH, REGEX, CUSTOM
    val pattern: String? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val errorMessage: String = ""
)

data class MappingProfile(
    val id: Long = 0,
    val name: String,
    val description: String,
    val datasetId: Long,
    val mappings: List<FieldMapping>,
    val successIndicator: SuccessIndicator?,
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = false
)

data class SuccessIndicator(
    val type: String, // PAGE_CHANGE, URL_CHANGE, SUCCESS_MESSAGE, ELEMENT_APPEARS, CUSTOM
    val value: String, // URL pattern, message text, element selector, etc.
    val timeout: Long = 5000 // milliseconds
)
