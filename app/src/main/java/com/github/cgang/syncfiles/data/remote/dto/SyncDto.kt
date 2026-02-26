package com.github.cgang.syncfiles.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangeLogDto(
    @SerialName("repo_id") val repo_id: Int,
    val operation: String,
    val path: String,
    @SerialName("old_path") val old_path: String? = null,
    @SerialName("user_id") val user_id: Int,
    val version: String,
    val timestamp: String
)

@Serializable
data class ChangesResponse(
    val version: String,
    val changes: List<ChangeLogDto>,
    val changed: Int
)

@Serializable
data class RepositoryVersionResponse(
    val version: String,
    val vector: String,
    val timestamp: String
)

@Serializable
data class BeginUploadResponse(
    @SerialName("upload_id") val upload_id: String,
    @SerialName("total_chunks") val total_chunks: Int,
    @SerialName("chunk_size") val chunk_size: Long,
    @SerialName("uploaded_chunks") val uploaded_chunks: List<Int>
)

@Serializable
data class UploadChunkResponse(
    val success: Boolean,
    val message: String? = null
)

@Serializable
data class FinalizeUploadResponse(
    val etag: String,
    val size: Long
)

@Serializable
data class CancelUploadResponse(
    val success: Boolean,
    val message: String? = null
)
