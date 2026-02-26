package com.github.cgang.syncfiles.domain.usecase

import com.github.cgang.syncfiles.sync.db.SyncStateEntity
import com.github.cgang.syncfiles.sync.db.SyncStatus
import com.github.cgang.syncfiles.sync.repository.SyncRepository
import com.github.cgang.syncfiles.sync.repository.SyncStats
import com.github.cgang.syncfiles.sync.upload.UploadManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for syncing DCIM files to server
 */
@Singleton
class SyncDcimUseCase @Inject constructor(
    private val syncRepository: SyncRepository,
    private val uploadManager: UploadManager
) {
    
    /**
     * Get sync state as a Flow
     */
    fun getSyncState(): Flow<List<SyncStateEntity>> {
        return syncRepository.getSyncStateStream()
    }
    
    /**
     * Get pending count as Flow
     */
    fun getPendingCount(): Flow<Int> {
        return syncRepository.getPendingCount()
    }
    
    /**
     * Get sync stats
     */
    suspend fun getSyncStats(): SyncStats {
        return syncRepository.getSyncStats()
    }
    
    /**
     * Scan DCIM folder and queue new files for upload
     * @param repoName Server repository name (default: "dcim")
     * @return Number of new files queued
     */
    suspend fun scanForNewFiles(repoName: String = "dcim"): Result<Int> {
        return syncRepository.scanAndQueueNewFiles(repoName)
    }
    
    /**
     * Upload pending files
     * @param repoName Server repository name
     * @param maxFiles Maximum number of files to upload in this session
     * @param onFileProgress Callback for individual file progress (fileId, progress 0-1)
     * @param onFileComplete Callback when a file is complete (fileId, success, error)
     */
    suspend fun uploadPendingFiles(
        repoName: String,
        maxFiles: Int = 10,
        onFileProgress: (Long, Float) -> Unit = { _, _ -> },
        onFileComplete: (Long, Boolean, String?) -> Unit = { _, _, _ -> }
    ): Result<UploadSummary> {
        val pendingFiles = syncRepository.getPendingItems(maxFiles)
        
        if (pendingFiles.isEmpty()) {
            return Result.success(UploadSummary(0, 0, 0, emptyList<UploadResult>()))
        }
        
        val results = mutableListOf<UploadResult>()
        var successCount = 0
        var failCount = 0
        
        for (file in pendingFiles) {
            try {
                // Mark as uploading
                syncRepository.markAsUploading(file.id)
                
                // Upload file
                val localFile = java.io.File(file.localPath)
                if (!localFile.exists()) {
                    syncRepository.markAsFailed(file.id, "File not found")
                    failCount++
                    onFileComplete(file.id, false, "File not found")
                    continue
                }
                
                val uploadResult = uploadManager.uploadFile(
                    repoName = repoName,
                    serverPath = file.serverPath,
                    file = localFile,
                    onProgress = { progress ->
                        onFileProgress(file.id, progress)
                    }
                )
                
                uploadResult.onSuccess { result ->
                    // Mark as synced
                    syncRepository.markAsSynced(file.id, 0, result.etag)
                    successCount++
                    results.add(
                        UploadResult(
                            id = file.id,
                            fileName = file.fileName,
                            success = true,
                            serverPath = result.serverPath
                        )
                    )
                    onFileComplete(file.id, true, null)
                }.onFailure { error ->
                    syncRepository.markAsFailed(file.id, error.message ?: "Upload failed")
                    failCount++
                    results.add(
                        UploadResult(
                            id = file.id,
                            fileName = file.fileName,
                            success = false,
                            error = error.message
                        )
                    )
                    onFileComplete(file.id, false, error.message)
                }
            } catch (e: Exception) {
                syncRepository.markAsFailed(file.id, e.message ?: "Unknown error")
                failCount++
                results.add(
                    UploadResult(
                        id = file.id,
                        fileName = file.fileName,
                        success = false,
                        error = e.message
                    )
                )
                onFileComplete(file.id, false, e.message)
            }
        }
        
        return Result.success(
            UploadSummary(
                total = pendingFiles.size,
                successCount = successCount,
                failedCount = failCount,
                results = results
            )
        )
    }
    
    /**
     * Retry failed uploads
     */
    suspend fun retryFailedFiles(repoName: String): Result<Int> {
        val failedFiles = syncRepository.getPendingItems(100)
            .filter { it.status == SyncStatus.FAILED }
        
        if (failedFiles.isEmpty()) {
            return Result.success(0)
        }
        
        // Reset status to pending
        failedFiles.forEach { file ->
            syncRepository.markAsUploading(file.id)
            // This will be picked up by next uploadPendingFiles call
        }
        
        return Result.success(failedFiles.size)
    }
    
    /**
     * Cancel sync for a specific file
     */
    suspend fun cancelFileSync(localPath: String) {
        syncRepository.getSyncStateByPath(localPath)?.let {
            // Mark for deletion or keep as failed
        }
    }
}

/**
 * Summary of an upload session
 */
data class UploadSummary(
    val total: Int,
    val successCount: Int,
    val failedCount: Int,
    val results: List<UploadResult>
) {
    val progress: Float
        get() = if (total == 0) 0f else (successCount.toFloat() / total)
}

/**
 * Result of uploading a single file
 */
data class UploadResult(
    val id: Long,
    val fileName: String,
    val success: Boolean,
    val serverPath: String? = null,
    val error: String? = null
)
