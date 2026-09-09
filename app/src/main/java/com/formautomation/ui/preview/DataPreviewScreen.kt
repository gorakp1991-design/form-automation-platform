package com.formautomation.ui.preview

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DataPreviewScreen(
    datasetId: Long,
    onProceedToMapping: (datasetId: Long) -> Unit
) {
    var selectedRows by remember { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Data Preview",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Statistics card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Total Rows: 1500")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Duplicates: 12")
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("Empty Cells: 45")
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Selected: ${selectedRows.size}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Table preview
        Text(
            text = "Preview (first 10 rows)",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.dp, MaterialTheme.colorScheme.outline)
                .verticalScroll(rememberScrollState())
        ) {
            // Table will be rendered here
            Text("Table preview placeholder", modifier = Modifier.padding(16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                onClick = { onProceedToMapping(datasetId) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Proceed to Mapping")
            }
        }
    }
}
