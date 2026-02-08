package com.github.cgang.syncfiles.domain.model

import java.util.Date

data class FileObject(
    val serverId: Int,
    val repoName: String,
    val path: String,
    val name: String,
    val parentId: Long = 0,
    val size: Long = 0,
    val isDir: Boolean = false,
    val mimeType: String? = null,
    val etag: String? = null,
    val modTime: Date = Date(),
    val createdAt: Date = Date(),
    val isDownloaded: Boolean = false,
    val isDirty: Boolean = false
) {
    val displayName: String
        get() = if (path.isEmpty()) "/" else path.substringAfterLast('/')

    val parentPath: String
        get() = if (path.isEmpty()) "" else path.substringBeforeLast('/', "")

    val extension: String?
        get() = if (isDir || name.contains('.')) {
            name.substringAfterLast('.', "")
        } else null
}
