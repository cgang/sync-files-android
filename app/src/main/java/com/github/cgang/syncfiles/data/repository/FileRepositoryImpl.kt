package com.github.cgang.syncfiles.data.repository

import android.content.Context
import com.github.cgang.syncfiles.data.mapper.toDomainModels
import com.github.cgang.syncfiles.data.remote.api.FileHubApi
import com.github.cgang.syncfiles.domain.model.FileObject
import com.github.cgang.syncfiles.domain.model.SyncResult
import com.github.cgang.syncfiles.security.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import java.io.File

interface FileRepository {
    fun observeFiles(repoName: String, offset: Int = 0, limit: Int = 100): Flow<List<FileObject>>
    suspend fun getFiles(repoName: String, path: String, forceRefresh: Boolean = false): List<FileObject>
    suspend fun downloadFile(repoName: String, path: String, outputPath: String): Result<Unit>
    suspend fun getFile(repoName: String, path: String): FileObject?
    suspend fun getFileInfo(repoName: String, path: String): FileObject?
}

class FileRepositoryImpl(
    private val context: Context,
    private val okHttpClient: OkHttpClient,
    private val sessionManager: SessionManager,
    private val fileHubApi: FileHubApi
) : FileRepository {

    override fun observeFiles(repoName: String, offset: Int, limit: Int): Flow<List<FileObject>> {
        return flow {
            val files = getFiles(repoName, "/", false)
            emit(files)
        }
    }

    override suspend fun getFiles(repoName: String, path: String, forceRefresh: Boolean): List<FileObject> {
        // Update API base URL
        fileHubApi.setBaseUrl(sessionManager.serverUrl)
        
        // Call list directory API
        return fileHubApi.listDirectory(repoName, path)
            .map { response ->
                response.items.toDomainModels(repoName)
            }
            .getOrDefault(emptyList())
    }

    override suspend fun downloadFile(repoName: String, path: String, outputPath: String): Result<Unit> {
        // Update API base URL
        fileHubApi.setBaseUrl(sessionManager.serverUrl)
        
        val outputFile = File(outputPath)
        
        // Ensure parent directory exists
        outputFile.parentFile?.mkdirs()
        
        // Call download API
        return fileHubApi.downloadFile(repoName, path, outputFile)
    }

    override suspend fun getFile(repoName: String, path: String): FileObject? {
        // Update API base URL
        fileHubApi.setBaseUrl(sessionManager.serverUrl)
        
        val files = getFiles(repoName, path)
        return files.firstOrNull { it.path == path }
    }

    override suspend fun getFileInfo(repoName: String, path: String): FileObject? {
        // Update API base URL
        fileHubApi.setBaseUrl(sessionManager.serverUrl)
        
        // Note: The API returns generic response, we may need to fetch from list
        val result = fileHubApi.getFileInfo(repoName, path)
        return if (result.isSuccess) {
            // For now, fetch from list directory
            getFiles(repoName, path).firstOrNull { it.path == path }
        } else {
            null
        }
    }
}
