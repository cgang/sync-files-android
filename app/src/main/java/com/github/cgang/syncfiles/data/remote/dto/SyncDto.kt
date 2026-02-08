package com.github.cgang.syncfiles.data.remote.dto

data class ChangeLogDto(
    val repo_id: Int,
    val operation: String,
    val path: String,
    val old_path: String? = null,
    val user_id: Int,
    val version: String,
    val timestamp: String
)

data class ChangesResponse(
    val version: String,
    val changes: List<ChangeLogDto>,
    val changed: Int
)

data class RepositoryVersionResponse(
    val version: String,
    val vector: String,
    val timestamp: String
)

data class BeginUploadResponse(
    val upload_id: String,
    val total_chunks: Int,
    val chunk_size: Long,
    val uploaded_chunks: List<Int>
)

data class UploadChunkResponse(
    val success: Boolean,
    val message: String? = null
)

data class FinalizeUploadResponse(
    val etag: String,
    val size: Long
)

data class CancelUploadResponse(
    val success: Boolean,
    val message: String? = null
)
