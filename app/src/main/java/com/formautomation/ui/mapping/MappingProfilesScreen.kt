package com.formautomation.ui.mapping

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MappingProfilesScreen(
    onProfileSelected: (profileId: Long) -> Unit,
    onCreateNew: () -> Unit
) {
    var profiles by remember { mutableStateOf(listOf<ProfileUIModel>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mapping Profiles",
                style = MaterialTheme.typography.headlineLarge
            )
            FloatingActionButton(
                onClick = onCreateNew,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create new profile")
            }
        }

        if (profiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No profiles yet. Create one to get started.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn {
                items(profiles) { profile ->
                    ProfileCard(
                        profile = profile,
                        onSelect = { onProfileSelected(profile.id) },
                        onEdit = { /* TODO */ },
                        onDelete = {
                            profiles = profiles.filter { it.id != profile.id }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ProfileCard(
    profile: ProfileUIModel,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit profile", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete profile", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(
                text = profile.description,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row {
                Text(
                    text = "Mappings: ${profile.mappingCount}",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Created: ${profile.createdAt}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

data class ProfileUIModel(
    val id: Long,
    val name: String,
    val description: String,
    val mappingCount: Int,
    val createdAt: String
)
