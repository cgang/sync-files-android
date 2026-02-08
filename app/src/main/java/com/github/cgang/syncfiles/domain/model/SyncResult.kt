package com.github.cgang.syncfiles.domain.model

data class SyncResult(
    val success: Boolean,
    val filesCreated: Int = 0,
    val filesModified: Int = 0,
    val filesDeleted: Int = 0,
    val filesMoved: Int = 0,
    val syncedVersion: String? = null,
    val error: String? = null
) {
    val totalChanges: Int
        get() = filesCreated + filesModified + filesDeleted + filesMoved

    companion object {
        fun success(
            filesCreated: Int = 0,
            filesModified: Int = 0,
            filesDeleted: Int = 0,
            filesMoved: Int = 0,
            syncedVersion: String? = null
        ) = SyncResult(
            success = true,
            filesCreated = filesCreated,
            filesModified = filesModified,
            filesDeleted = filesDeleted,
            filesMoved = filesMoved,
            syncedVersion = syncedVersion
        )

        fun failure(error: String) = SyncResult(
            success = false,
            error = error
        )
    }
}
