package com.github.cgang.syncfiles.presentation.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.cgang.syncfiles.domain.usecase.DownloadFileUseCase
import com.github.cgang.syncfiles.domain.usecase.GetFilesUseCase
import com.github.cgang.syncfiles.domain.model.FileObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FileBrowserUiState {
    object Idle : FileBrowserUiState()
    object Loading : FileBrowserUiState()
    data class Success(val files: List<FileObject>) : FileBrowserUiState()
    data class Error(val message: String) : FileBrowserUiState()
}

@HiltViewModel
class FileBrowserViewModel @Inject constructor(
    private val getFilesUseCase: GetFilesUseCase,
    private val downloadFileUseCase: DownloadFileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FileBrowserUiState>(FileBrowserUiState.Idle)
    val uiState: StateFlow<FileBrowserUiState> = _uiState.asStateFlow()

    var currentRepo: String = ""
        private set

    var currentPath: String = ""
        private set

    fun loadFiles(repo: String, path: String, forceRefresh: Boolean = false) {
        currentRepo = repo
        currentPath = path

        viewModelScope.launch {
            _uiState.value = FileBrowserUiState.Loading

            try {
                val files = getFilesUseCase(repo, path, forceRefresh)
                _uiState.value = FileBrowserUiState.Success(files)
            } catch (e: Exception) {
                _uiState.value = FileBrowserUiState.Error(
                    e.message ?: "Failed to load files"
                )
            }
        }
    }

    fun downloadFile(file: FileObject) {
        viewModelScope.launch {
            val outputPath = "/sdcard/Download/${file.name}"

            downloadFileUseCase(file.repoName, file.path, outputPath)
                .onSuccess {
                }
                .onFailure { error ->
                    _uiState.value = FileBrowserUiState.Error(
                        error.message ?: "Download failed"
                    )
                }
        }
    }

    fun navigateTo(path: String): String {
        return if (path == "..") {
            if (currentPath.isEmpty() || currentPath == "/") {
                "/"
            } else {
                currentPath.substringBeforeLast('/', "")
            }
        } else {
            if (currentPath.isEmpty()) path else "$currentPath/$path"
        }
    }

    fun resetState() {
        _uiState.value = FileBrowserUiState.Idle
    }
}
