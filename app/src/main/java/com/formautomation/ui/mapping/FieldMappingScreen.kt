package com.formautomation.ui.mapping

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FieldMappingScreen(
    datasetId: Long,
    spreadsheetColumns: List<String>,
    onMappingComplete: (mappingProfileId: Long) -> Unit
) {
    var profileName by remember { mutableStateOf("") }
    var profileDescription by remember { mutableStateOf("") }
    var mappings by remember { mutableStateOf(listOf<MappingUIModel>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var successIndicatorType by remember { mutableStateOf("PAGE_CHANGE") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Field Mapping",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Profile info section
        OutlinedTextField(
            value = profileName,
            onValueChange = { profileName = it },
            label = { Text("Profile Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = profileDescription,
            onValueChange = { profileDescription = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            maxLines = 2
        )

        // Mappings section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mappings (${mappings.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add mapping")
                    }
                }

                LazyColumn {
                    items(mappings) { mapping ->
                        MappingRow(
                            mapping = mapping,
                            onDelete = {
                                mappings = mappings.filter { it.id != mapping.id }
                            }
                        )
                        Divider()
                    }
                }
            }
        }

        // Success indicator section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Success Indicator",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "How will the app know when save is successful?",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = successIndicatorType,
                    onValueChange = { successIndicatorType = it },
                    label = { Text("Indicator Type") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    readOnly = false
                )
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* Back */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Back")
            }
            Button(
                onClick = { /* onMappingComplete(0L) */ },
                enabled = mappings.isNotEmpty() && profileName.isNotEmpty(),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Profile")
            }
        }
    }

    // Add mapping dialog
    if (showAddDialog) {
        AddMappingDialog(
            spreadsheetColumns = spreadsheetColumns,
            onConfirm = { newMapping ->
                mappings = mappings + newMapping.copy(id = mappings.size.toLong())
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun MappingRow(
    mapping: MappingUIModel,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${mapping.spreadsheetColumn} → ${mapping.formFieldName}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Type: ${mapping.fieldType}",
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete mapping")
        }
    }
}

@Composable
fun AddMappingDialog(
    spreadsheetColumns: List<String>,
    onConfirm: (MappingUIModel) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColumn by remember { mutableStateOf(spreadsheetColumns.firstOrNull() ?: "") }
    var formFieldName by remember { mutableStateOf("") }
    var fieldType by remember { mutableStateOf("TEXT") }
    var isRequired by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Field Mapping") },
        text = {
            Column {
                OutlinedTextField(
                    value = selectedColumn,
                    onValueChange = { selectedColumn = it },
                    label = { Text("Spreadsheet Column") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = formFieldName,
                    onValueChange = { formFieldName = it },
                    label = { Text("Form Field Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = fieldType,
                    onValueChange = { fieldType = it },
                    label = { Text("Field Type") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newMapping = MappingUIModel(
                        id = 0L,
                        spreadsheetColumn = selectedColumn,
                        formFieldId = formFieldName.lowercase().replace(" ", "_"),
                        formFieldName = formFieldName,
                        fieldType = fieldType,
                        isRequired = isRequired
                    )
                    onConfirm(newMapping)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class MappingUIModel(
    val id: Long,
    val spreadsheetColumn: String,
    val formFieldId: String,
    val formFieldName: String,
    val fieldType: String,
    val isRequired: Boolean = false
)
