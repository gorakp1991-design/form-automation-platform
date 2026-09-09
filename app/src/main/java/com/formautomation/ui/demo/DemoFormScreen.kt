package com.formautomation.ui.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun DemoFormScreen(
    onFormComplete: (formData: Map<String, String>) -> Unit
) {
    var formName by remember { mutableStateOf("") }
    var formPhone by remember { mutableStateOf("") }
    var formEmail by remember { mutableStateOf("") }
    var formDob by remember { mutableStateOf("") }
    var formAddress by remember { mutableStateOf("") }
    var formId by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }

    var isSaving by remember { mutableStateOf(false) }
    var saveSuccess by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            delay(1000)
            onFormComplete(
                mapOf(
                    "name" to formName,
                    "phone" to formPhone,
                    "email" to formEmail,
                    "dob" to formDob,
                    "address" to formAddress,
                    "id" to formId
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Demo Form - Test Automation",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Instructions card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "ℹ️ This form tests the automation engine",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Fill all fields to submit. Automation will practice inputting data here.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Name field
        FormTextField(
            value = formName,
            onValueChange = {
                formName = it
                nameError = null
            },
            label = "Full Name *",
            error = nameError,
            placeholder = "e.g., John Doe",
            keyboardType = KeyboardType.Text
        )

        // Phone field
        FormTextField(
            value = formPhone,
            onValueChange = {
                formPhone = it
                phoneError = null
            },
            label = "Phone Number *",
            error = phoneError,
            placeholder = "e.g., 9876543210",
            keyboardType = KeyboardType.Phone
        )

        // Email field
        FormTextField(
            value = formEmail,
            onValueChange = {
                formEmail = it
                emailError = null
            },
            label = "Email Address *",
            error = emailError,
            placeholder = "e.g., john@example.com",
            keyboardType = KeyboardType.Email
        )

        // DOB field
        FormTextField(
            value = formDob,
            onValueChange = {
                formDob = it
                dobError = null
            },
            label = "Date of Birth *",
            error = dobError,
            placeholder = "YYYY-MM-DD",
            keyboardType = KeyboardType.Number
        )

        // Address field
        FormTextField(
            value = formAddress,
            onValueChange = {
                formAddress = it
                addressError = null
            },
            label = "Address *",
            error = addressError,
            placeholder = "e.g., 123 Main Street",
            keyboardType = KeyboardType.Text,
            singleLine = false,
            maxLines = 3
        )

        // ID field (optional)
        FormTextField(
            value = formId,
            onValueChange = { formId = it },
            label = "ID (Optional)",
            error = null,
            placeholder = "e.g., ID12345",
            keyboardType = KeyboardType.Text
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Status messages
        if (saveSuccess) {
            SuccessMessageCard()
        }

        if (saveError != null) {
            ErrorMessageCard(message = saveError ?: "Unknown error")
        }

        // Save button
        Button(
            onClick = {
                // Validate form
                var hasErrors = false

                if (formName.isEmpty()) {
                    nameError = "Name is required"
                    hasErrors = true
                }

                if (formPhone.isEmpty()) {
                    phoneError = "Phone is required"
                    hasErrors = true
                } else if (!isValidPhone(formPhone)) {
                    phoneError = "Phone must be at least 10 digits"
                    hasErrors = true
                }

                if (formEmail.isEmpty()) {
                    emailError = "Email is required"
                    hasErrors = true
                } else if (!isValidEmail(formEmail)) {
                    emailError = "Invalid email format"
                    hasErrors = true
                }

                if (formDob.isEmpty()) {
                    dobError = "DOB is required"
                    hasErrors = true
                } else if (!isValidDate(formDob)) {
                    dobError = "Use YYYY-MM-DD format"
                    hasErrors = true
                }

                if (formAddress.isEmpty()) {
                    addressError = "Address is required"
                    hasErrors = true
                }

                if (!hasErrors) {
                    isSaving = true
                    // Simulate save delay
                    // In real app, this would be an actual network call
                }
            },
            enabled = !isSaving && !saveSuccess,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Saving...")
            } else if (saveSuccess) {
                Text("✓ Saved Successfully")
            } else {
                Text("SAVE")
            }
        }

        // Simulate actual save after delay
        LaunchedEffect(isSaving) {
            if (isSaving) {
                delay(1500) // Simulate network delay
                saveSuccess = true
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form state display (for debugging)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Form State (Debug)",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = "Fields Filled: ${countFilledFields(formName, formPhone, formEmail, formDob, formAddress)}/5",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Status: ${if (saveSuccess) "✓ Submitted" else if (isSaving) "Saving..." else "Ready"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    placeholder: String,
    keyboardType: KeyboardType,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (error != null) Modifier.border(
                        width = 2.dp,
                        color = Color.Red,
                        shape = RoundedCornerShape(4.dp)
                    ) else Modifier
                ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = singleLine,
            maxLines = maxLines,
            isError = error != null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error != null) Color.Red else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (error != null) Color.Red else Color.Gray
            )
        )
        if (error != null) {
            Text(
                text = "⚠️ $error",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SuccessMessageCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFC8E6C9))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Success",
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Form submitted successfully!",
                color = Color(0xFF2E7D32),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ErrorMessageCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = "Error",
                tint = Color(0xFFC62828),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                color = Color(0xFFC62828),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
    return emailRegex.matches(email)
}

fun isValidPhone(phone: String): Boolean {
    val phoneRegex = "^[0-9\\-\\+\\s\\(\\)]+$".toRegex()
    return phoneRegex.matches(phone) && phone.filter { it.isDigit() }.length >= 10
}

fun isValidDate(date: String): Boolean {
    val dateRegex = "^\\d{4}-\\d{2}-\\d{2}$".toRegex()
    return dateRegex.matches(date)
}

fun countFilledFields(vararg fields: String): Int {
    return fields.count { it.isNotEmpty() }
}
