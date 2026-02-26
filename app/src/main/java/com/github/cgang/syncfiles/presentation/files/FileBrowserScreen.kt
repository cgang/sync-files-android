package com.github.cgang.syncfiles.presentation.files

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.cgang.syncfiles.domain.model.FileObject
import com.github.cgang.syncfiles.presentation.components.ErrorView
import com.github.cgang.syncfiles.presentation.components.FileCard
import com.github.cgang.syncfiles.presentation.components.EmptyFolderView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    repoName: String,
    initialPath: String = "/",
    onLogout: () -> Unit,
    onNavigateToSync: () -> Unit,
    viewModel: FileBrowserViewModel
) {
    var currentPath by remember { mutableStateOf(initialPath) }
    val uiState by viewModel.uiState.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(repoName, currentPath) {
        viewModel.loadFiles(repoName, currentPath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = repoName,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = if (currentPath.isEmpty()) "Home" else currentPath.substringAfterLast('/'),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val newPath = viewModel.navigateTo("..")
                            if (newPath != currentPath) {
                                currentPath = newPath
                            }
                        },
                        enabled = currentPath.isNotEmpty()
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.loadFiles(repoName, currentPath, forceRefresh = true)
                        }
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("DCIM Sync") },
                            onClick = {
                                showMenu = false
                                onNavigateToSync()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Sync, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showMenu = false
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Logout, contentDescription = null)
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is FileBrowserUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is FileBrowserUiState.Success -> {
                val files = state.files

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (currentPath.isNotEmpty()) {
                        item {
                            FileCard(
                                file = FileObject(
                                    serverId = -1,
                                    repoName = repoName,
                                    path = "..",
                                    name = "..",
                                    isDir = true,
                                    size = 0
                                ),
                                onClick = {
                                    currentPath = viewModel.navigateTo("..")
                                }
                            )
                        }
                    }

                    if (files.isEmpty()) {
                        item {
                            EmptyFolderView()
                        }
                    } else {
                        items(files, key = { it.path }) { file ->
                            FileCard(
                                file = file,
                                onClick = {
                                    if (file.isDir) {
                                        currentPath = file.path
                                    } else {
                                        viewModel.downloadFile(file)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            is FileBrowserUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = {
                        viewModel.loadFiles(repoName, currentPath, forceRefresh = true)
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            FileBrowserUiState.Idle -> {
            }
        }
    }
}
