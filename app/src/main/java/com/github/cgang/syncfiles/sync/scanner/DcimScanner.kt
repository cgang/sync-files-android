package com.github.cgang.syncfiles.sync.scanner

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.github.cgang.syncfiles.sync.model.LocalMediaFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Scans the DCIM folder for photos and videos
 */
class DcimScanner(
    private val context: Context
) {
    
    /**
     * Scan DCIM folder for media files
     * @param limit Maximum number of files to return
     * @param sinceTimestamp Only return files modified after this timestamp (epoch ms)
     */
    suspend fun scanMediaFiles(
        limit: Int = 100,
        sinceTimestamp: Long = 0L
    ): Result<List<LocalMediaFile>> = withContext(Dispatchers.IO) {
        try {
            val mediaFiles = mutableListOf<LocalMediaFile>()
            
            // Query images
            val images = queryMediaStore(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                sinceTimestamp,
                limit
            )
            mediaFiles.addAll(images)
            
            // Query videos if we haven't reached the limit
            if (mediaFiles.size < limit) {
                val remaining = limit - mediaFiles.size
                val videos = queryMediaStore(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    sinceTimestamp,
                    remaining
                )
                mediaFiles.addAll(videos)
            }
            
            // Sort by date taken (newest first)
            mediaFiles.sortByDescending { it.dateTaken }
            
            Result.success(mediaFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun queryMediaStore(
        uri: Uri,
        sinceTimestamp: Long,
        limit: Int
    ): List<LocalMediaFile> {
        val files = mutableListOf<LocalMediaFile>()
        
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_TAKEN,
            MediaStore.MediaColumns.DATE_MODIFIED
        )
        
        val selection = if (sinceTimestamp > 0) {
            "${MediaStore.MediaColumns.DATE_TAKEN} > ?"
        } else {
            null
        }
        
        val selectionArgs = if (sinceTimestamp > 0) {
            arrayOf(sinceTimestamp.toString())
        } else {
            null
        }
        
        val sortOrder = "${MediaStore.MediaColumns.DATE_TAKEN} DESC"
        
        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN)
            
            var count = 0
            while (cursor.moveToNext() && count < limit) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val path = cursor.getString(dataColumn)
                val size = cursor.getLong(sizeColumn)
                val mimeType = cursor.getString(mimeColumn)
                val dateTaken = cursor.getLong(dateTakenColumn)
                
                // Skip if file doesn't exist on disk
                val file = File(path)
                if (!file.exists()) continue
                
                // Calculate checksum (first 8 bytes for quick comparison)
                val checksum = calculateQuickChecksum(file)
                
                files.add(
                    LocalMediaFile(
                        id = id,
                        name = name,
                        path = path,
                        size = size,
                        mimeType = mimeType,
                        dateTaken = dateTaken,
                        checksum = checksum,
                        isVideo = mimeType.startsWith("video/")
                    )
                )
                count++
            }
        }
        
        return files
    }
    
    /**
     * Calculate a quick checksum based on file size and first/last bytes
     * This is used for change detection, not cryptographic security
     */
    private fun calculateQuickChecksum(file: File): String {
        if (!file.exists() || !file.canRead()) {
            return "0"
        }
        
        try {
            val size = file.length()
            val firstBytes = ByteArray(minOf(8, size.toInt()))
            
            file.inputStream().use { input ->
                input.read(firstBytes)
            }
            
            val firstHex = firstBytes.joinToString("") { "%02x".format(it) }
            val lastModified = file.lastModified()
            
            return "${size}_${firstHex}_${lastModified}"
        } catch (e: Exception) {
            return "0"
        }
    }
    
    /**
     * Get the DCIM folder path
     */
    fun getDcimPath(): String {
        return context.getExternalFilesDir(null)?.parent + "/DCIM"
    }
    
    /**
     * Get total count of media files (for progress tracking)
     */
    suspend fun getMediaCount(sinceTimestamp: Long = 0L): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var count = 0
            
            // Count images
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns._ID),
                if (sinceTimestamp > 0) "${MediaStore.MediaColumns.DATE_TAKEN} > ?" else null,
                if (sinceTimestamp > 0) arrayOf(sinceTimestamp.toString()) else null,
                null
            )?.use { cursor ->
                count = cursor.count
            }
            
            // Add video count
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.MediaColumns._ID),
                if (sinceTimestamp > 0) "${MediaStore.MediaColumns.DATE_TAKEN} > ?" else null,
                if (sinceTimestamp > 0) arrayOf(sinceTimestamp.toString()) else null,
                null
            )?.use { cursor ->
                count += cursor.count
            }
            
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
