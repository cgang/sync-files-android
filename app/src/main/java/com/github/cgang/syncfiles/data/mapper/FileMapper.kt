package com.github.cgang.syncfiles.data.mapper

import com.github.cgang.syncfiles.data.remote.dto.FileDto
import com.github.cgang.syncfiles.domain.model.ChangeLog
import com.github.cgang.syncfiles.domain.model.FileObject
import com.github.cgang.syncfiles.util.DateUtils
import java.util.Date

fun FileDto.toDomainModel(repoName: String): FileObject {
    return FileObject(
        serverId = id,
        repoName = repoName,
        path = path,
        name = name,
        parentId = parent_id?.toLong() ?: 0,
        size = size,
        isDir = is_dir,
        mimeType = mime_type,
        etag = checksum,
        modTime = DateUtils.parseIsoDate(mod_time),
        createdAt = DateUtils.parseIsoDate(created_at),
        isDownloaded = false,
        isDirty = false
    )
}

fun List<FileDto>.toDomainModels(repoName: String): List<FileObject> {
    return map { it.toDomainModel(repoName) }
}

fun com.github.cgang.syncfiles.data.remote.dto.ChangeLogDto.toDomainModel(): ChangeLog {
    return ChangeLog(
        repoId = repo_id,
        operation = operation,
        path = path,
        oldPath = old_path,
        userId = user_id,
        version = version,
        timestamp = DateUtils.parseIsoDate(timestamp)
    )
}

fun List<com.github.cgang.syncfiles.data.remote.dto.ChangeLogDto>.toDomainModels(): List<ChangeLog> {
    return map { it.toDomainModel() }
}
