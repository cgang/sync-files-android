package com.github.cgang.syncfiles.data.remote.dto

import java.util.Date

data class FileDto(
    val id: Int,
    val parent_id: Int? = null,
    val owner_id: Int,
    val repo_id: Int,
    val name: String,
    val path: String,
    val mime_type: String? = null,
    val size: Long = 0,
    val mod_time: String,
    val checksum: String? = null,
    val created_at: String,
    val updated_at: String,
    val is_dir: Boolean = false
)

data class ListDirectoryResponse(
    val items: List<FileDto>,
    val total: Long,
    val offset: Int,
    val limit: Int,
    val has_more: Boolean
)

data class FileInfoResponse(
    val exists: Boolean,
    val info: FileDto? = null,
    val message: String? = null
)

data class UploadResponse(
    val etag: String,
    val version: String,
    val size: Long
)

data class GenericResponse(
    val success: Boolean,
    val message: String? = null
)
