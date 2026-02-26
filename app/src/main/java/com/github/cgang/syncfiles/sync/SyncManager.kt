package com.github.cgang.syncfiles.sync

import android.content.Context
import androidx.work.*
import com.github.cgang.syncfiles.sync.worker.DcimSyncWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for scheduling and monitoring sync jobs
 */
@Singleton
class SyncManager @Inject constructor(
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule periodic background sync
     * @param intervalHours Sync interval in hours (default: 1)
     */
    fun schedulePeriodicSync(intervalHours: Long = 1L) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<DcimSyncWorker>(
            intervalHours,
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            DcimSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            syncRequest
        )
    }

    /**
     * Cancel periodic sync
     */
    fun cancelPeriodicSync() {
        workManager.cancelUniqueWork(DcimSyncWorker.WORK_NAME)
    }

    /**
     * Trigger an immediate one-time sync
     */
    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<DcimSyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueue(syncRequest)
    }

    /**
     * Get current sync work info
     */
    fun getSyncWorkInfo(): List<WorkInfo> {
        return workManager.getWorkInfosForUniqueWork(DcimSyncWorker.WORK_NAME).get()
    }

    /**
     * Check if periodic sync is scheduled
     */
    fun isPeriodicSyncScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(DcimSyncWorker.WORK_NAME).get()
        return workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    }
}
