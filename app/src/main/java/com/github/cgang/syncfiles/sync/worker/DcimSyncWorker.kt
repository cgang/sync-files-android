package com.github.cgang.syncfiles.sync.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.github.cgang.syncfiles.R
import com.github.cgang.syncfiles.domain.usecase.SyncDcimUseCase
import com.github.cgang.syncfiles.presentation.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Background worker for DCIM sync
 */
@HiltWorker
class DcimSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncDcimUseCase: SyncDcimUseCase
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "dcim_sync"
        const val CHANNEL_ID = "dcim_sync_channel"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        try {
            // Scan for new files first
            val scanResult = syncDcimUseCase.scanForNewFiles()
            val newFilesCount = scanResult.getOrDefault(0)

            if (newFilesCount > 0) {
                // Upload pending files
                val uploadResult = syncDcimUseCase.uploadPendingFiles(
                    repoName = "dcim",
                    maxFiles = 50
                )

                uploadResult.onSuccess { summary ->
                    if (summary.successCount > 0) {
                        showCompletionNotification(summary.successCount, summary.failedCount)
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DCIM Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows progress of photo/video sync to server"
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Syncing photos...")
            .setContentText("Uploading media files to server")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setProgress(100, 0, true)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification(successCount: Int, failedCount: Int) {
        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val message = if (failedCount > 0) {
            "$successCount uploaded, $failedCount failed"
        } else {
            "$successCount files uploaded successfully"
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Sync complete")
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
