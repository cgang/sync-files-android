package com.github.cgang.syncfiles.data.remote.dto

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String? = null
)

data class LogoutResponse(
    val success: Boolean
)

data class ServerStatusResponse(
    val status: String,
    val database: String? = null,
    val hasUsers: Boolean = false
)

data class UserInfoResponse(
    val message: String,
    val dav: String? = null
)

data class SetupRequest(
    val username: String,
    val password: String,
    val email: String
)

data class SetupResponse(
    val success: Boolean,
    val message: String? = null
)
