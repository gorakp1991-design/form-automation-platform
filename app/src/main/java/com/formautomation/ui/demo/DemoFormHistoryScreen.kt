package com.formautomation.ui.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class FormSubmissionRecord(
    val recordNumber: Int,
    val timestamp: String,
    val status: String, // SUCCESS, FAILED, PENDING
    val data: Map<String, String>
)

@Composable
fun DemoFormHistoryScreen(
    submissions: List<FormSubmissionRecord>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Form Submission History",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (submissions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No submissions yet", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn {
                items(submissions.size) { index ->
                    SubmissionCard(record = submissions[index])
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SubmissionCard(record: FormSubmissionRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (record.status) {
                "SUCCESS" -> Color(0xFFC8E6C9)
                "FAILED" -> Color(0xFFFFCDD2)
                else -> Color(0xFFFFF9C4)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = when (record.status) {
                            "SUCCESS" -> Icons.Filled.CheckCircle
                            "FAILED" -> Icons.Filled.Error
                            else -> Icons.Filled.Refresh
                        },
                        contentDescription = record.status,
                        tint = when (record.status) {
                            "SUCCESS" -> Color(0xFF2E7D32)
                            "FAILED" -> Color(0xFFC62828)
                            else -> Color(0xFFF57F17)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Record #${record.recordNumber}",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = record.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = when (record.status) {
                        "SUCCESS" -> Color(0xFF2E7D32)
                        "FAILED" -> Color(0xFFC62828)
                        else -> Color(0xFFF57F17)
                    }
                )
            }

            Text(
                text = "Time: ${record.timestamp}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Show submitted data
            record.data.forEach { (key, value) ->
                Text(
                    text = "$key: $value",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}
