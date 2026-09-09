package com.formautomation.ui.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.formautomation.data.repository.DemoFormRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class DemoFormUIState(
    val isLoading: Boolean = false,
    val successCount: Int = 0,
    val failedCount: Int = 0,
    val lastSubmissionTime: String = "",
    val error: String? = null
)

class DemoFormViewModel(
    private val demoFormRepository: DemoFormRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(DemoFormUIState())
    val uiState: StateFlow<DemoFormUIState> = _uiState

    fun submitForm(formData: Map<String, String>) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val submissionNumber = (_uiState.value.successCount + _uiState.value.failedCount) + 1
                val result = demoFormRepository.submitForm(submissionNumber, formData)

                result.onSuccess {
                    val successCount = demoFormRepository.getSuccessCount()
                    val failedCount = demoFormRepository.getFailedCount()
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val currentTime = timeFormat.format(Date())

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successCount = successCount,
                        failedCount = failedCount,
                        lastSubmissionTime = currentTime,
                        error = null
                    )
                }

                result.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun recordFailedSubmission(formData: Map<String, String>, errorMessage: String) {
        viewModelScope.launch {
            try {
                val submissionNumber = (_uiState.value.successCount + _uiState.value.failedCount) + 1
                demoFormRepository.recordFailedSubmission(submissionNumber, formData, errorMessage)

                val failedCount = demoFormRepository.getFailedCount()
                _uiState.value = _uiState.value.copy(
                    failedCount = failedCount,
                    error = errorMessage
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun clearAllSubmissions() {
        viewModelScope.launch {
            demoFormRepository.clearAllSubmissions()
            _uiState.value = DemoFormUIState()
        }
    }
}
