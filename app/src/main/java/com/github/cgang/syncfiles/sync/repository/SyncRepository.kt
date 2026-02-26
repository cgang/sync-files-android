package com.github.cgang.syncfiles.sync.repository

import com.github.cgang.syncfiles.sync.db.SyncStateDao
import com.github.cgang.syncfiles.sync.db.SyncStateEntity
import com.github.cgang.syncfiles.sync.db.SyncStatus
import com.github.cgang.syncfiles.sync.model.LocalMediaFile
import com.github.cgang.syncfiles.sync.scanner.DcimScanner
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing DCIM sync operations
 */
@Singleton
class SyncRepository @Inject constructor(
    private val syncStateDao: SyncStateDao,
    private val dcimScanner: DcimScanner
) {
    
    /**
     * Get sync state as Flow
     */
    fun getSyncStateStream(): Flow<List<SyncStateEntity>> {
        return syncStateDao.getAll()
    }
    
    /**
     * Get pending files count
     */
    fun getPendingCount(): Flow<Int> {
        return syncStateDao.countByStatus(SyncStatus.PENDING)
    }
    
    /**
     * Get uploading files count
     */
    fun getUploadingCount(): Flow<Int> {
        return syncStateDao.countByStatus(SyncStatus.UPLOADING)
    }
    
    /**
     * Get synced files count
     */
    fun getSyncedCount(): Flow<Int> {
        return syncStateDao.countByStatus(SyncStatus.SYNCED)
    }
    
    /**
     * Get failed files count
     */
    fun getFailedCount(): Flow<Int> {
        return syncStateDao.countByStatus(SyncStatus.FAILED)
    }
    
    /**
     * Get all pending items for upload
     */
    suspend fun getPendingItems(limit: Int = 10): List<SyncStateEntity> {
        return syncStateDao.getPendingWithLimit(
            listOf(SyncStatus.PENDING, SyncStatus.FAILED),
            limit
        )
    }
    
    /**
     * Scan DCIM and queue new files for sync
     * @param repoName The server repository name to upload to
     * @return Number of new files queued
     */
    suspend fun scanAndQueueNewFiles(repoName: String = "dcim"): Result<Int> {
        return try {
            // Get last sync timestamp (most recent synced file)
            val lastSyncedTime = syncStateDao.getAllSync()
                .filter { it.status == SyncStatus.SYNCED }
                .maxOfOrNull { it.dateTaken } ?: 0L
            
            // Scan for new media files
            val scanResult = dcimScanner.scanMediaFiles(
                limit = 500,
                sinceTimestamp = lastSyncedTime
            )
            
            scanResult.getOrNull()?.let { mediaFiles ->
                // Filter out already synced files
                val newFiles = mediaFiles.filter { file ->
                    syncStateDao.getByChecksum(file.checksum) == null
                }
                
                // Create sync state entities
                val syncStates = newFiles.map { file ->
                    SyncStateEntity(
                        localPath = file.path,
                        fileName = file.name,
                        serverPath = file.getServerPath(repoName),
                        fileSize = file.size,
                        checksum = file.checksum,
                        mimeType = file.mimeType,
                        dateTaken = file.dateTaken,
                        status = SyncStatus.PENDING,
                        isVideo = file.isVideo
                    )
                }
                
                // Insert into database
                if (syncStates.isNotEmpty()) {
                    syncStateDao.insertAll(syncStates)
                }
                
                Result.success(syncStates.size)
            } ?: Result.failure(Exception("Failed to scan media files"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Mark a file as uploading
     */
    suspend fun markAsUploading(id: Long) {
        syncStateDao.updateStatus(id, SyncStatus.UPLOADING)
    }
    
    /**
     * Mark a file as successfully synced
     */
    suspend fun markAsSynced(id: Long, serverFileId: Long, serverEtag: String?) {
        syncStateDao.updateSynced(id, SyncStatus.SYNCED, serverFileId, serverEtag)
    }
    
    /**
     * Mark a file as failed
     */
    suspend fun markAsFailed(id: Long, error: String) {
        syncStateDao.updateFailed(id, SyncStatus.FAILED, error)
    }
    
    /**
     * Get sync state by local path
     */
    suspend fun getSyncStateByPath(path: String): SyncStateEntity? {
        return syncStateDao.getByLocalPath(path)
    }
    
    /**
     * Check if a file is already synced
     */
    suspend fun isFileSynced(checksum: String): Boolean {
        return syncStateDao.getByChecksum(checksum)?.status == SyncStatus.SYNCED
    }
    
    /**
     * Get all sync stats
     */
    suspend fun getSyncStats(): SyncStats {
        val all = syncStateDao.getAllSync()
        return SyncStats(
            total = all.size,
            pending = all.count { it.status == SyncStatus.PENDING },
            uploading = all.count { it.status == SyncStatus.UPLOADING },
            synced = all.count { it.status == SyncStatus.SYNCED },
            failed = all.count { it.status == SyncStatus.FAILED || it.status == SyncStatus.ERROR }
        )
    }
}

/**
 * Sync statistics data class
 */
data class SyncStats(
    val total: Int,
    val pending: Int,
    val uploading: Int,
    val synced: Int,
    val failed: Int
) {
    val progress: Float
        get() = if (total == 0) 0f else (synced.toFloat() / total) * 100f
}
