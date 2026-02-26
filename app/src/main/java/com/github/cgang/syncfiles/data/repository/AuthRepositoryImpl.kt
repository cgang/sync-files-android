package com.github.cgang.syncfiles.data.repository

import com.github.cgang.syncfiles.data.remote.api.FileHubApi
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
    private val sessionManager: SessionManager,
    private val fileHubApi: FileHubApi
) : AuthRepository {

    override suspend fun setup(username: String, password: String, email: String): Result<User> {
        return try {
            // Update API base URL with server URL from session manager
            fileHubApi.setBaseUrl(sessionManager.serverUrl)
            
            // Call setup API
            fileHubApi.setup(username, password, email)
                .map {
                    // Save credentials
                    credentialManager.saveCredentials(username, password)
                    
                    // Create user object
                    User(
                        id = 0,
                        username = username,
                        email = email,
                        firstName = null,
                        lastName = null,
                        isActive = true,
                        isAdmin = true
                    )
                }
                .onSuccess { user ->
                    sessionManager.setCurrentUser(user)
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            // Update API base URL with server URL from session manager
            fileHubApi.setBaseUrl(sessionManager.serverUrl)
            
            // Call login API
            fileHubApi.login(username, password)
                .map {
                    // Save credentials
                    credentialManager.saveCredentials(username, password)
                    
                    // Create user object
                    User(
                        id = 0,
                        username = username,
                        email = "",
                        firstName = null,
                        lastName = null,
                        isActive = true,
                        isAdmin = false
                    )
                }
                .onSuccess { user ->
                    sessionManager.setCurrentUser(user)
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            fileHubApi.logout()
                .map {
                    credentialManager.clearCredentials()
                    sessionManager.clearCurrentUser()
                    Unit
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkStatus(): Result<Boolean> {
        return try {
            // Update API base URL with server URL from session manager
            fileHubApi.setBaseUrl(sessionManager.serverUrl)
            
            fileHubApi.checkServerStatus()
                .map { response ->
                    response.status == "ok" || response.status == "ready"
                }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): User? {
        return sessionManager.getCurrentUser()
    }
}
