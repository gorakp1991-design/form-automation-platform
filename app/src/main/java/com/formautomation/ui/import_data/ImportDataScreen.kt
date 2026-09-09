package com.formautomation.ui.import_data

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ImportDataScreen(
    onDatasetImported: (datasetId: Long) -> Unit
) {
    var selectedFile by remember { mutableStateOf<String?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var importProgress by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Import Data",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // File selection button
        Button(
            onClick = { /* TODO: Open file picker */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Select CSV File")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected file info
        if (selectedFile != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = "Selected: $selectedFile",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Import button
        Button(
            onClick = { isImporting = true },
            enabled = !isImporting && selectedFile != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isImporting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Importing... $importProgress%")
            } else {
                Text("Import")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Recent imports
        Text(
            text = "Recent Imports",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
