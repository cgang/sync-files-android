package com.github.cgang.syncfiles.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.cgang.syncfiles.domain.usecase.LoginUseCase
import com.github.cgang.syncfiles.security.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val username: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val serverUrl: String
        get() = sessionManager.serverUrl

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Please enter username and password")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            loginUseCase(username, password)
                .onSuccess { user ->
                    _uiState.value = LoginUiState.Success(user.username)
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "Login failed"
                    )
                }
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}
