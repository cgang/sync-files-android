package com.github.cgang.syncfiles.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
data class FileDto(
    val id: Int,
    @SerialName("parent_id") val parent_id: Int? = null,
    @SerialName("owner_id") val owner_id: Int,
    @SerialName("repo_id") val repo_id: Int,
    val name: String,
    val path: String,
    @SerialName("mime_type") val mime_type: String? = null,
    val size: Long = 0,
    @SerialName("mod_time") val mod_time: String,
    val checksum: String? = null,
    @SerialName("created_at") val created_at: String,
    @SerialName("updated_at") val updated_at: String,
    @SerialName("is_dir") val is_dir: Boolean = false
)

@Serializable
data class ListDirectoryResponse(
    val items: List<FileDto>,
    val total: Long,
    val offset: Int,
    val limit: Int,
    @SerialName("has_more") val has_more: Boolean
)

@Serializable
data class FileInfoResponse(
    val exists: Boolean,
    val info: FileDto? = null,
    val message: String? = null
)

@Serializable
data class UploadResponse(
    val etag: String,
    val version: String,
    val size: Long
)

@Serializable
data class GenericResponse(
    val success: Boolean,
    val message: String? = null
)
