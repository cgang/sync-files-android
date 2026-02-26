package com.github.cgang.syncfiles.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val success: Boolean,
    val message: String? = null
)

@Serializable
data class LogoutResponse(
    val success: Boolean
)

@Serializable
data class ServerStatusResponse(
    val status: String,
    val database: String? = null,
    @SerialName("has_users") val hasUsers: Boolean = false
)

@Serializable
data class UserInfoResponse(
    val message: String,
    val dav: String? = null
)

@Serializable
data class SetupRequest(
    val username: String,
    val password: String,
    val email: String
)

@Serializable
data class SetupResponse(
    val success: Boolean,
    val message: String? = null
)
