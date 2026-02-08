package com.github.cgang.syncfiles.domain.usecase

import com.github.cgang.syncfiles.data.repository.FileRepository
import com.github.cgang.syncfiles.domain.model.FileObject
import kotlinx.coroutines.flow.Flow

class GetFilesUseCase(
    private val fileRepository: FileRepository
) {
    fun observe(repoName: String, offset: Int = 0, limit: Int = 100): Flow<List<FileObject>> {
        return fileRepository.observeFiles(repoName, offset, limit)
    }

    suspend operator fun invoke(repoName: String, path: String, forceRefresh: Boolean = false): List<FileObject> {
        return fileRepository.getFiles(repoName, path, forceRefresh)
    }
}

class DownloadFileUseCase(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(repoName: String, path: String, outputPath: String): Result<Unit> {
        return fileRepository.downloadFile(repoName, path, outputPath)
    }
}

class GetFileInfoUseCase(
    private val fileRepository: FileRepository
) {
    suspend operator fun invoke(repoName: String, path: String): FileObject? {
        return fileRepository.getFileInfo(repoName, path)
    }
}
