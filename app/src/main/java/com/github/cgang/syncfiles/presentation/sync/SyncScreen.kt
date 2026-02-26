package com.github.cgang.syncfiles.presentation.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.cgang.syncfiles.sync.db.SyncStateEntity
import com.github.cgang.syncfiles.sync.repository.SyncStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyncViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()
    val isPeriodicSyncEnabled by viewModel.isPeriodicSyncEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DCIM Sync") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.triggerSync() }) {
                        Icon(
                            if (isSyncing) Icons.Default.Stop else Icons.Default.Refresh,
                            contentDescription = if (isSyncing) "Stop" else "Sync now"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Sync Status Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Sync Status",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    when (val state = uiState) {
                        is SyncUiState.Success -> {
                            SyncStatsContent(stats = state.stats)
                        }
                        else -> {
                            Text("Loading...")
                        }
                    }

                    // Progress bar during sync
                    if (isSyncing) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = syncProgress,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Syncing... ${(syncProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Periodic sync toggle
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-sync every hour",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = isPeriodicSyncEnabled,
                            onCheckedChange = { viewModel.togglePeriodicSync(it) }
                        )
                    }
                }
            }

            // Pending Files List
            Text(
                text = "Pending Files",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            when (val state = uiState) {
                is SyncUiState.Success -> {
                    if (state.pendingFiles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No pending files",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.pendingFiles, key = { it.id }) { file ->
                                SyncFileCard(file = file)
                            }
                        }
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatsContent(stats: SyncStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(
            label = "Total",
            value = stats.total.toString(),
            icon = Icons.Default.Folder
        )
        StatItem(
            label = "Synced",
            value = stats.synced.toString(),
            icon = Icons.Default.CheckCircle,
            color = MaterialTheme.colorScheme.primary
        )
        StatItem(
            label = "Pending",
            value = stats.pending.toString(),
            icon = Icons.Default.Schedule,
            color = MaterialTheme.colorScheme.tertiary
        )
        StatItem(
            label = "Failed",
            value = stats.failed.toString(),
            icon = Icons.Default.Error,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SyncFileCard(file: SyncStateEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (file.isVideo) Icons.Default.VideoFile else Icons.Default.Image,
                contentDescription = null,
                tint = when (file.status) {
                    com.github.cgang.syncfiles.sync.db.SyncStatus.SYNCED -> MaterialTheme.colorScheme.primary
                    com.github.cgang.syncfiles.sync.db.SyncStatus.FAILED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.fileName,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatFileSize(file.fileSize),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status indicator
            when (file.status) {
                com.github.cgang.syncfiles.sync.db.SyncStatus.PENDING -> {
                    AssistChip(
                        onClick = { },
                        label = { Text("Pending", style = MaterialTheme.typography.labelSmall) }
                    )
                }
                com.github.cgang.syncfiles.sync.db.SyncStatus.UPLOADING -> {
                    AssistChip(
                        onClick = { },
                        label = { Text("Uploading", style = MaterialTheme.typography.labelSmall) }
                    )
                }
                com.github.cgang.syncfiles.sync.db.SyncStatus.SYNCED -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Synced",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                com.github.cgang.syncfiles.sync.db.SyncStatus.FAILED,
                com.github.cgang.syncfiles.sync.db.SyncStatus.ERROR -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Failed",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(
        java.util.Locale.US,
        "%.1f %s",
        bytes / Math.pow(1024.0, digitGroups.toDouble()),
        units[digitGroups]
    )
}
