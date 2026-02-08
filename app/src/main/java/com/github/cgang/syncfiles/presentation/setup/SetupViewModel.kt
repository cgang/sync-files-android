package com.github.cgang.syncfiles.presentation.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.cgang.syncfiles.domain.usecase.CheckServerStatusUseCase
import com.github.cgang.syncfiles.security.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SetupUiState {
    object Idle : SetupUiState()
    object Validating : SetupUiState()
    object Success : SetupUiState()
    data class Error(val message: String) : SetupUiState()
}

class SetupViewModel(
    private val checkServerStatusUseCase: CheckServerStatusUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SetupUiState>(SetupUiState.Idle)
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    var serverUrl: String
        get() = sessionManager.serverUrl
        set(value) {
            sessionManager.serverUrl = value
        }

    fun validateServer(url: String) {
        viewModelScope.launch {
            sessionManager.serverUrl = url
            _uiState.value = SetupUiState.Validating

            checkServerStatusUseCase()
                .onSuccess { isConnected ->
                    if (isConnected) {
                        _uiState.value = SetupUiState.Success
                    } else {
                        _uiState.value = SetupUiState.Error("Server is not ready")
                    }
                }
                .onFailure { error ->
                    _uiState.value = SetupUiState.Error(
                        error.message ?: "Failed to connect to server"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = SetupUiState.Idle
    }
}
