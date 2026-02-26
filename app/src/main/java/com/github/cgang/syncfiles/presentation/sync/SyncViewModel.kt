package com.github.cgang.syncfiles.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.cgang.syncfiles.domain.usecase.SyncDcimUseCase
import com.github.cgang.syncfiles.sync.SyncManager
import com.github.cgang.syncfiles.sync.db.SyncStateEntity
import com.github.cgang.syncfiles.sync.repository.SyncStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SyncUiState {
    object Loading : SyncUiState()
    data class Success(
        val stats: SyncStats,
        val pendingFiles: List<SyncStateEntity> = emptyList()
    ) : SyncUiState()
    data class Error(val message: String) : SyncUiState()
}

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val syncDcimUseCase: SyncDcimUseCase,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SyncUiState>(SyncUiState.Loading)
    val uiState: StateFlow<SyncUiState> = _uiState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _isPeriodicSyncEnabled = MutableStateFlow(false)
    val isPeriodicSyncEnabled: StateFlow<Boolean> = _isPeriodicSyncEnabled.asStateFlow()

    init {
        loadSyncState()
        checkPeriodicSyncStatus()
    }

    fun loadSyncState() {
        viewModelScope.launch {
            try {
                val stats = syncDcimUseCase.getSyncStats()
                _uiState.value = SyncUiState.Success(stats)
            } catch (e: Exception) {
                _uiState.value = SyncUiState.Error(e.message ?: "Failed to load sync state")
            }
        }
    }

    fun triggerSync() {
        if (_isSyncing.value) return

        viewModelScope.launch {
            _isSyncing.value = true
            _syncProgress.value = 0f

            try {
                // Scan for new files
                val scanResult = syncDcimUseCase.scanForNewFiles()
                scanResult.onSuccess { newCount ->
                    // Upload pending files
                    val uploadResult = syncDcimUseCase.uploadPendingFiles(
                        repoName = "dcim",
                        maxFiles = 50,
                        onFileProgress = { fileId, progress ->
                            _syncProgress.value = progress
                        }
                    )

                    uploadResult.onSuccess { summary ->
                        _syncProgress.value = 1f
                        loadSyncState() // Refresh state
                    }
                }
            } catch (e: Exception) {
                _uiState.value = SyncUiState.Error(e.message ?: "Sync failed")
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun togglePeriodicSync(enabled: Boolean) {
        if (enabled) {
            syncManager.schedulePeriodicSync(intervalHours = 1)
        } else {
            syncManager.cancelPeriodicSync()
        }
        _isPeriodicSyncEnabled.value = enabled
    }

    fun checkPeriodicSyncStatus() {
        _isPeriodicSyncEnabled.value = syncManager.isPeriodicSyncScheduled()
    }

    fun retryFailedFiles() {
        viewModelScope.launch {
            syncDcimUseCase.retryFailedFiles("dcim")
            loadSyncState()
        }
    }
}
