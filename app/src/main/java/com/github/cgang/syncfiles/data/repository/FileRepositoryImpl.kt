package com.github.cgang.syncfiles.data.repository

import com.github.cgang.syncfiles.domain.model.FileObject
import com.github.cgang.syncfiles.domain.model.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.io.File

interface FileRepository {
    fun observeFiles(repoName: String, offset: Int = 0, limit: Int = 100): Flow<List<FileObject>>
    suspend fun getFiles(repoName: String, path: String, forceRefresh: Boolean = false): List<FileObject>
    suspend fun downloadFile(repoName: String, path: String, outputPath: String): Result<Unit>
    suspend fun getFile(repoName: String, path: String): FileObject?
    suspend fun getFileInfo(repoName: String, path: String): FileObject?
}

class FileRepositoryImpl : FileRepository {

    override fun observeFiles(repoName: String, offset: Int, limit: Int): Flow<List<FileObject>> {
        return flowOf(emptyList())
    }

    override suspend fun getFiles(repoName: String, path: String, forceRefresh: Boolean): List<FileObject> {
        // TODO: Implement actual network call with OkHttp
        return emptyList() // Stubbed for now
    }

    override suspend fun downloadFile(repoName: String, path: String, outputPath: String): Result<Unit> {
        // TODO: Implement actual network call with OkHttp
        return Result.success(Unit) // Stubbed for now
    }

    override suspend fun getFile(repoName: String, path: String): FileObject? {
        // TODO: Implement actual network call with OkHttp
        return null // Stubbed for now
    }

    override suspend fun getFileInfo(repoName: String, path: String): FileObject? {
        // TODO: Implement actual network call with OkHttp
        return null // Stubbed for now
    }
}
