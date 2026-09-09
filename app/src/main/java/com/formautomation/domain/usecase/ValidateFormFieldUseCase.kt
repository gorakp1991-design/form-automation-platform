package com.formautomation.domain.usecase

import com.formautomation.domain.model.FieldMapping
import com.formautomation.domain.validator.FieldValidator
import com.formautomation.domain.validator.ValidationResult

class ValidateFormFieldUseCase(
    private val validator: FieldValidator
) {
    fun execute(mapping: FieldMapping, value: String): ValidationResult {
        return validator.validate(mapping, value)
    }
}

class ValidateAllFieldsUseCase(
    private val validator: FieldValidator
) {
    fun execute(mappings: List<FieldMapping>, values: Map<String, String>): Map<String, String?> {
        val errors = mutableMapOf<String, String?>()

        mappings.forEach { mapping ->
            val value = values[mapping.spreadsheetColumn] ?: ""
            val result = validator.validate(mapping, value)
            errors[mapping.spreadsheetColumn] = if (result is ValidationResult.Invalid) {
                result.message
            } else {
                null
            }
        }

        return errors
    }
}
