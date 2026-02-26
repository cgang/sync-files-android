package com.github.cgang.syncfiles.sync.model

/**
 * Represents a local media file (photo or video) from DCIM folder
 */
data class LocalMediaFile(
    val id: Long,
    val name: String,
    val path: String,
    val size: Long,
    val mimeType: String,
    val dateTaken: Long,
    val checksum: String,
    val isVideo: Boolean
) {
    /**
     * Get file extension
     */
    val extension: String
        get() = name.substringAfterLast('.', "").lowercase()
    
    /**
     * Check if this is a photo (not video)
     */
    val isPhoto: Boolean
        get() = !isVideo
    
    /**
     * Get relative path for server upload
     */
    fun getServerPath(repoName: String): String {
        val yearMonth = java.text.SimpleDateFormat("yyyy/MM", java.util.Locale.US)
            .format(java.util.Date(dateTaken))
        return "/$repoName/$yearMonth/$name"
    }
}
