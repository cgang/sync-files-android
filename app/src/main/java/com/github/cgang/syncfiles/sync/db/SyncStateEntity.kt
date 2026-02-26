package com.github.cgang.syncfiles.sync.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity representing the sync state of a local file
 */
@Entity(
    tableName = "sync_state",
    indices = [
        Index(value = ["localPath"], unique = true),
        Index(value = ["serverPath"]),
        Index(value = ["status"])
    ]
)
data class SyncStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * Local file path
     */
    val localPath: String,
    
    /**
     * File name
     */
    val fileName: String,
    
    /**
     * Server path where file should be uploaded
     */
    val serverPath: String,
    
    /**
     * File size in bytes
     */
    val fileSize: Long,
    
    /**
     * File checksum for change detection
     */
    val checksum: String,
    
    /**
     * MIME type of the file
     */
    val mimeType: String,
    
    /**
     * When the file was taken/captured
     */
    val dateTaken: Long,
    
    /**
     * Whether this is a video file
     */
    val isVideo: Boolean = false,
    
    /**
     * Current sync status
     */
    val status: SyncStatus,
    
    /**
     * Number of retry attempts
     */
    val retryCount: Int = 0,
    
    /**
     * Last error message if failed
     */
    val lastError: String? = null,
    
    /**
     * When this record was created
     */
    val createdAt: Long = System.currentTimeMillis(),
    
    /**
     * When this record was last updated
     */
    val updatedAt: Long = System.currentTimeMillis(),
    
    /**
     * Server file ID after successful upload
     */
    val serverFileId: Long? = null,
    
    /**
     * Server ETag for the uploaded file
     */
    val serverEtag: String? = null
)

/**
 * Sync status enum
 */
enum class SyncStatus {
    /**
     * File is queued for upload
     */
    PENDING,
    
    /**
     * File is currently being uploaded
     */
    UPLOADING,
    
    /**
     * File was successfully uploaded
     */
    SYNCED,
    
    /**
     * Upload failed, will retry
     */
    FAILED,
    
    /**
     * Upload failed permanently, user intervention needed
     */
    ERROR
}
