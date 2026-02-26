package com.github.cgang.syncfiles.sync.upload

import com.github.cgang.syncfiles.constants.Config
import com.github.cgang.syncfiles.data.remote.api.FileHubApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles chunked file uploads with resume support
 */
@Singleton
class UploadManager @Inject constructor(
    private val fileHubApi: FileHubApi
) {
    
    /**
     * Upload a file with chunked transfer
     * @param repoName Repository name
     * @param serverPath Path on server
     * @param file Local file to upload
     * @param onProgress Progress callback (0.0 to 1.0)
     */
    suspend fun uploadFile(
        repoName: String,
        serverPath: String,
        file: File,
        onProgress: (Float) -> Unit = {}
    ): Result<UploadResult> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File does not exist: ${file.absolutePath}"))
            }
            
            val fileSize = file.length()
            
            // Calculate MD5 checksum
            val checksum = calculateMd5(file)
            
            // Begin upload session
            val beginResult = fileHubApi.beginUpload(repoName, serverPath, fileSize, checksum)
                .getOrElse { return@withContext Result.failure(it) }
            
            val uploadId = beginResult.upload_id
            val chunkSize = beginResult.chunk_size
            val totalChunks = beginResult.total_chunks
            val uploadedChunks = beginResult.uploaded_chunks.toMutableSet()
            
            // Upload missing chunks
            var uploadedBytes = uploadedChunks.sumOf { index ->
                minOf(chunkSize, fileSize - (index * chunkSize))
            }
            
            for (chunkIndex in 0 until totalChunks) {
                if (chunkIndex in uploadedChunks) {
                    // Chunk already uploaded (resume scenario)
                    continue
                }
                
                val offset = chunkIndex * chunkSize
                val bytesToRead = minOf(chunkSize, fileSize - offset)
                
                // Read chunk
                val chunkData = ByteArray(bytesToRead.toInt())
                FileInputStream(file).use { input ->
                    input.skip(offset)
                    input.read(chunkData)
                }
                
                // Upload chunk
                val chunkResult = fileHubApi.uploadChunk(repoName, uploadId, chunkIndex, chunkData)
                    .getOrElse { return@withContext Result.failure(it) }
                
                if (!chunkResult.success) {
                    return@withContext Result.failure(Exception(chunkResult.message ?: "Chunk upload failed"))
                }
                
                uploadedBytes += bytesToRead
                onProgress(uploadedBytes.toFloat() / fileSize)
            }
            
            // Finalize upload
            val finalizeResult = fileHubApi.finalizeUpload(repoName, uploadId)
                .getOrElse { return@withContext Result.failure(it) }
            
            Result.success(
                UploadResult(
                    serverPath = serverPath,
                    etag = finalizeResult.etag,
                    size = finalizeResult.size,
                    uploadId = uploadId
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cancel an in-progress upload
     */
    suspend fun cancelUpload(repoName: String, uploadId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            fileHubApi.cancelUpload(repoName, uploadId)
                .map { }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Calculate MD5 checksum of a file
     */
    private fun calculateMd5(file: File): String {
        val md5 = MessageDigest.getInstance("MD5")
        FileInputStream(file).use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                md5.update(buffer, 0, bytesRead)
            }
        }
        return md5.digest().joinToString("") { "%02x".format(it) }
    }
}

/**
 * Result of a successful upload
 */
data class UploadResult(
    val serverPath: String,
    val etag: String,
    val size: Long,
    val uploadId: String
)
