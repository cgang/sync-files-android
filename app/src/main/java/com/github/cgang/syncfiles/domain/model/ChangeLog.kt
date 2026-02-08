package com.github.cgang.syncfiles.domain.model

import java.util.Date

data class ChangeLog(
    val repoId: Int,
    val operation: String,
    val path: String,
    val oldPath: String? = null,
    val userId: Int,
    val version: String,
    val timestamp: Date = Date()
)

enum class ChangeOperation(val value: String) {
    CREATE("create"),
    MODIFY("modify"),
    DELETE("delete"),
    MOVE("move"),
    COPY("copy");

    companion object {
        fun from(value: String): ChangeOperation {
            return entries.find { it.value == value } ?: CREATE
        }
    }
}
