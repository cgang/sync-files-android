package com.github.cgang.syncfiles.data.remote.api

import com.github.cgang.syncfiles.constants.Config
import com.github.cgang.syncfiles.data.remote.dto.BeginUploadResponse
import com.github.cgang.syncfiles.data.remote.dto.CancelUploadResponse
import com.github.cgang.syncfiles.data.remote.dto.ChangesResponse
import com.github.cgang.syncfiles.data.remote.dto.FinalizeUploadResponse
import com.github.cgang.syncfiles.data.remote.dto.GenericResponse
import com.github.cgang.syncfiles.data.remote.dto.ListDirectoryResponse
import com.github.cgang.syncfiles.data.remote.dto.LoginRequest
import com.github.cgang.syncfiles.data.remote.dto.LoginResponse
import com.github.cgang.syncfiles.data.remote.dto.LogoutResponse
import com.github.cgang.syncfiles.data.remote.dto.RepositoryVersionResponse
import com.github.cgang.syncfiles.data.remote.dto.ServerStatusResponse
import com.github.cgang.syncfiles.data.remote.dto.SetupRequest
import com.github.cgang.syncfiles.data.remote.dto.UploadChunkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileHubApi @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private var baseUrl: String = Config.DEFAULT_SERVER_URL

    fun setBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }

    private fun getUrl(path: String): String {
        return "$baseUrl$path"
    }

    // ============== Authentication Endpoints ==============

    suspend fun checkServerStatus(): Result<ServerStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(getUrl("/api/status"))
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                    val dto = json.decodeFromString(ServerStatusResponse.serializer(), body)
                    Result.success(dto)
                } else {
                    Result.failure(Exception("Server status check failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val requestJson = json.encodeToString(LoginRequest.serializer(), LoginRequest(username, password))
            val request = Request.Builder()
                .url(getUrl("/api/auth/login"))
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                
                if (response.isSuccessful) {
                    val dto = json.decodeFromString(LoginResponse.serializer(), body)
                    if (dto.success) {
                        Result.success(dto)
                    } else {
                        Result.failure(Exception(dto.message ?: "Login failed"))
                    }
                } else {
                    Result.failure(Exception("Login failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setup(username: String, password: String, email: String): Result<SetupRequest> = withContext(Dispatchers.IO) {
        try {
            val requestJson = json.encodeToString(SetupRequest.serializer(), SetupRequest(username, password, email))
            val request = Request.Builder()
                .url(getUrl("/api/auth/setup"))
                .post(requestJson.toRequestBody("application/json".toMediaType()))
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                
                if (response.isSuccessful) {
                    val dto = json.decodeFromString(GenericResponse.serializer(), body)
                    if (dto.success) {
                        Result.success(SetupRequest(username, password, email))
                    } else {
                        Result.failure(Exception(dto.message ?: "Setup failed"))
                    }
                } else {
                    Result.failure(Exception("Setup failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<LogoutResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(getUrl("/api/auth/logout"))
                .post("".toRequestBody())
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                
                if (response.isSuccessful) {
                    val dto = json.decodeFromString(LogoutResponse.serializer(), body)
                    Result.success(dto)
                } else {
                    Result.failure(Exception("Logout failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserInfo(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(getUrl("/api/auth/userinfo"))
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Result.success(response.body?.string() ?: "Unknown user")
                } else {
                    Result.failure(Exception("Failed to get user info: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============== File Endpoints ==============

    suspend fun listDirectory(repoName: String, path: String, offset: Int = 0, limit: Int = 100): Result<ListDirectoryResponse> = withContext(Dispatchers.IO) {
        try {
            val encodedPath = java.net.URLEncoder.encode(path, "UTF-8")
            val url = getUrl("/api/files/$repoName/list?path=$encodedPath&offset=$offset&limit=$limit")
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                    val dto = json.decodeFromString(ListDirectoryResponse.serializer(), body)
                    Result.success(dto)
                } else {
                    Result.failure(Exception("Failed to list directory: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFileInfo(repoName: String, path: String): Result<GenericResponse> = withContext(Dispatchers.IO) {
        try {
            val encodedPath = java.net.URLEncoder.encode(path, "UTF-8")
            val url = getUrl("/api/files/$repoName/info?path=$encodedPath")
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                    val dto = json.decodeFromString(GenericResponse.serializer(), body)
                    Result.success(dto)
                } else {
                    Result.failure(Exception("Failed to get file info: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadFile(repoName: String, path: String, outputFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val encodedPath = java.net.URLEncoder.encode(path, "UTF-8")
            val url = getUrl("/api/files/$repoName/download?path=$encodedPath")
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.let { responseBody ->
                        FileOutputStream(outputFile).use { output ->
                            responseBody.byteStream().use { input ->
                                input.copyTo(output)
                            }
                        }
                        Result.success(Unit)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Download failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun beginUpload(
        repoName: String,
        path: String,
        fileSize: Long,
        checksum: String
    ): Result<BeginUploadResponse> = withContext(Dispatchers.IO) {
        try {
            val encodedPath = java.net.URLEncoder.encode(path, "UTF-8")
            val url = getUrl("/api/files/$repoName/upload/begin?path=$encodedPath&size=$fileSize&checksum=$checksum")
            
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody())
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                    val dto = json.decodeFromString(BeginUploadResponse.serializer(), body)
                    Result.success(dto)
                } else {
                    Result.failure(Exception("Begin upload failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadChunk(
        repoName: String,
        uploadId: String,
        chunkIndex: Int,
        chunkData: ByteArray
    ): Result<UploadChunkResponse> = withContext(Dispatchers.IO) {
        try {
            val url = getUrl("/api/files/$repoName/upload/chunk?upload_id=$uploadId&index=$chunkIndex")
            
            val request = Request.Builder()
                .url(url)
                .put(chunkData.toRequestBody("application/octet-stream".toMediaType()))
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                
                if (response.isSuccessful) {
                    val dto = json.decodeFromString(UploadChunkResponse.serializer(), body)
                    Result.success(dto)
                } else {
                    Result.failure(Exception("Upload chunk failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finalizeUpload(repoName: String, uploadId: String): Result<FinalizeUploadResponse> = withContext(Dispatchers.IO) {
        try {
            val url = getUrl("/api/files/$repoName/upload/finalize?upload_id=$uploadId")
            
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody())
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                    val dto = json.decodeFromString(FinalizeUploadResponse.serializer(), body)
                    Result.success(dto)
                } else {
                    Result.failure(Exception("Finalize upload failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelUpload(repoName: String, uploadId: String): Result<CancelUploadResponse> = withContext(Dispatchers.IO) {
        try {
            val url = getUrl("/api/files/$repoName/upload/cancel?upload_id=$uploadId")
            
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody())
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                    val dto = json.decodeFromString(CancelUploadResponse.serializer(), body)
                    Result.success(dto)
                } else {
                    Result.failure(Exception("Cancel upload failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============== Sync Endpoints ==============

    suspend fun getChanges(repoName: String, sinceVersion: String): Result<ChangesResponse> = withContext(Dispatchers.IO) {
        try {
            val url = getUrl("/api/sync/$repoName/changes?since=$sinceVersion")
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                    val dto = json.decodeFromString(ChangesResponse.serializer(), body)
                    Result.success(dto)
                } else {
                    Result.failure(Exception("Get changes failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRepositoryVersion(repoName: String): Result<RepositoryVersionResponse> = withContext(Dispatchers.IO) {
        try {
            val url = getUrl("/api/sync/$repoName/version")
            
            val request = Request.Builder()
                .url(url)
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response"))
                    val dto = json.decodeFromString(RepositoryVersionResponse.serializer(), body)
                    Result.success(dto)
                } else {
                    Result.failure(Exception("Get version failed: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
