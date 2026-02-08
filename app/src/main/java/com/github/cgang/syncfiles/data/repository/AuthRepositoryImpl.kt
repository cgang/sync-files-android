package com.github.cgang.syncfiles.data.repository

import com.github.cgang.syncfiles.domain.model.User
import com.github.cgang.syncfiles.security.CredentialManager
import com.github.cgang.syncfiles.security.SessionManager

interface AuthRepository {
    suspend fun setup(username: String, password: String, email: String): Result<User>
    suspend fun login(username: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun checkStatus(): Result<Boolean>
    fun getCurrentUser(): User?
}

class AuthRepositoryImpl(
    private val credentialManager: CredentialManager,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun setup(username: String, password: String, email: String): Result<User> {
        // TODO: Implement actual network call with OkHttp
        return try {
            credentialManager.saveCredentials(username, password)

            val user = User(
                id = 0,
                username = username,
                email = email,
                firstName = null,
                lastName = null,
                isActive = true,
                isAdmin = true
            )

            sessionManager.setCurrentUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(username: String, password: String): Result<User> {
        // TODO: Implement actual network call with OkHttp
        return try {
            credentialManager.saveCredentials(username, password)

            val user = User(
                id = 0,
                username = username,
                email = "",
                firstName = null,
                lastName = null,
                isActive = true,
                isAdmin = false
            )

            sessionManager.setCurrentUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        // TODO: Implement actual network call with OkHttp
        credentialManager.clearCredentials()
        sessionManager.clearCurrentUser()
        return Result.success(Unit)
    }

    override suspend fun checkStatus(): Result<Boolean> {
        // TODO: Implement actual network call with OkHttp
        return Result.success(true) // Stubbed for now
    }

    override fun getCurrentUser(): User? {
        return sessionManager.getCurrentUser()
    }
}
