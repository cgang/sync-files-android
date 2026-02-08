package com.github.cgang.syncfiles.domain.usecase

import com.github.cgang.syncfiles.data.repository.AuthRepository
import com.github.cgang.syncfiles.domain.model.User

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return authRepository.login(username, password)
    }
}

class SetupUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String, email: String): Result<User> {
        return authRepository.setup(username, password, email)
    }
}

class LogoutUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}

class CheckServerStatusUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return authRepository.checkStatus()
    }
}
