package com.github.cgang.syncfiles.domain.model

import java.util.Date

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val isActive: Boolean = true,
    val isAdmin: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val lastLogin: Date? = null
) {
    val displayName: String
        get() = if (firstName != null && lastName != null) {
            "$firstName $lastName"
        } else {
            username
        }
}
