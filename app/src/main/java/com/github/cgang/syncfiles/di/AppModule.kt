package com.github.cgang.syncfiles.di

import android.content.Context
import com.github.cgang.syncfiles.data.remote.api.FileHubApi
import com.github.cgang.syncfiles.data.remote.interceptor.AuthInterceptor
import com.github.cgang.syncfiles.data.repository.AuthRepository
import com.github.cgang.syncfiles.data.repository.AuthRepositoryImpl
import com.github.cgang.syncfiles.data.repository.FileRepository
import com.github.cgang.syncfiles.data.repository.FileRepositoryImpl
import com.github.cgang.syncfiles.domain.usecase.*
import com.github.cgang.syncfiles.security.CredentialManager
import com.github.cgang.syncfiles.security.SessionManager
import com.github.cgang.syncfiles.util.NetworkMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideCredentialManager(
        @ApplicationContext context: Context
    ): CredentialManager {
        return CredentialManager(context)
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context,
        credentialManager: CredentialManager
    ): SessionManager {
        return SessionManager(context, credentialManager)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context
    ): NetworkMonitor {
        return NetworkMonitor(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        @ApplicationContext context: Context
    ): AuthInterceptor {
        return AuthInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideFileHubApi(okHttpClient: OkHttpClient): FileHubApi {
        return FileHubApi(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        credentialManager: CredentialManager,
        sessionManager: SessionManager,
        fileHubApi: FileHubApi
    ): AuthRepository {
        return AuthRepositoryImpl(credentialManager, sessionManager, fileHubApi)
    }

    @Provides
    @Singleton
    fun provideFileRepository(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        sessionManager: SessionManager,
        fileHubApi: FileHubApi
    ): FileRepository {
        return FileRepositoryImpl(context, okHttpClient, sessionManager, fileHubApi)
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideSetupUseCase(authRepository: AuthRepository): SetupUseCase {
        return SetupUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideLogoutUseCase(authRepository: AuthRepository): LogoutUseCase {
        return LogoutUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideCheckServerStatusUseCase(authRepository: AuthRepository): CheckServerStatusUseCase {
        return CheckServerStatusUseCase(authRepository)
    }

    @Provides
    @Singleton
    fun provideGetFilesUseCase(fileRepository: FileRepository): GetFilesUseCase {
        return GetFilesUseCase(fileRepository)
    }

    @Provides
    @Singleton
    fun provideDownloadFileUseCase(fileRepository: FileRepository): DownloadFileUseCase {
        return DownloadFileUseCase(fileRepository)
    }

    @Provides
    @Singleton
    fun provideGetFileInfoUseCase(fileRepository: FileRepository): GetFileInfoUseCase {
        return GetFileInfoUseCase(fileRepository)
    }
}
