package com.formautomation.domain.validator

import com.formautomation.domain.model.FieldMapping
import com.formautomation.domain.model.ValidationRule

class FieldValidator {
    fun validate(mapping: FieldMapping, value: String): ValidationResult {
        // Check required
        if (mapping.isRequired && value.isEmpty()) {
            return ValidationResult.Invalid("${mapping.formFieldName} is required")
        }

        if (value.isEmpty()) {
            return ValidationResult.Valid
        }

        // Check field-specific validations
        return when (mapping.fieldType) {
            "EMAIL" -> validateEmail(value, mapping.validation)
            "PHONE" -> validatePhone(value, mapping.validation)
            "DATE" -> validateDate(value, mapping.validation)
            "NUMBER" -> validateNumber(value, mapping.validation)
            else -> validateGeneric(value, mapping.validation)
        }
    }

    private fun validateEmail(value: String, rule: ValidationRule?): ValidationResult {
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
        return if (emailRegex.matches(value)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(rule?.errorMessage ?: "Invalid email format")
        }
    }

    private fun validatePhone(value: String, rule: ValidationRule?): ValidationResult {
        val phoneRegex = "^[0-9\\-\\+\\s\\(\\)]+$".toRegex()
        return if (phoneRegex.matches(value) && value.length >= 10) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(rule?.errorMessage ?: "Invalid phone format")
        }
    }

    private fun validateDate(value: String, rule: ValidationRule?): ValidationResult {
        val dateRegex = "^\\d{4}-\\d{2}-\\d{2}$|^\\d{2}/\\d{2}/\\d{4}$".toRegex()
        return if (dateRegex.matches(value)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(rule?.errorMessage ?: "Invalid date format")
        }
    }

    private fun validateNumber(value: String, rule: ValidationRule?): ValidationResult {
        return if (value.toDoubleOrNull() != null) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(rule?.errorMessage ?: "Invalid number format")
        }
    }

    private fun validateGeneric(value: String, rule: ValidationRule?): ValidationResult {
        if (rule == null) return ValidationResult.Valid

        return when (rule.type) {
            "LENGTH" -> {
                if (rule.minLength != null && value.length < rule.minLength) {
                    ValidationResult.Invalid("Minimum length is ${rule.minLength}")
                } else if (rule.maxLength != null && value.length > rule.maxLength) {
                    ValidationResult.Invalid("Maximum length is ${rule.maxLength}")
                } else {
                    ValidationResult.Valid
                }
            }
            "REGEX" -> {
                if (rule.pattern != null && !rule.pattern.toRegex().matches(value)) {
                    ValidationResult.Invalid(rule.errorMessage)
                } else {
                    ValidationResult.Valid
                }
            }
            else -> ValidationResult.Valid
        }
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}
