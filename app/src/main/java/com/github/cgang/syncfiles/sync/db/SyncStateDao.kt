package com.github.cgang.syncfiles.sync.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStateDao {
    
    @Query("SELECT * FROM sync_state WHERE status = :status ORDER BY dateTaken DESC")
    fun getByStatus(status: SyncStatus): Flow<List<SyncStateEntity>>
    
    @Query("SELECT * FROM sync_state WHERE status = :status ORDER BY dateTaken DESC")
    suspend fun getByStatusSync(status: SyncStatus): List<SyncStateEntity>
    
    @Query("SELECT * FROM sync_state WHERE localPath = :path LIMIT 1")
    suspend fun getByLocalPath(path: String): SyncStateEntity?
    
    @Query("SELECT * FROM sync_state WHERE checksum = :checksum LIMIT 1")
    suspend fun getByChecksum(checksum: String): SyncStateEntity?
    
    @Query("SELECT * FROM sync_state ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<SyncStateEntity>>
    
    @Query("SELECT * FROM sync_state ORDER BY updatedAt DESC")
    suspend fun getAllSync(): List<SyncStateEntity>
    
    @Query("SELECT COUNT(*) FROM sync_state WHERE status = :status")
    fun countByStatus(status: SyncStatus): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM sync_state WHERE status = :status")
    suspend fun countByStatusSync(status: SyncStatus): Int
    
    @Query("SELECT * FROM sync_state WHERE status IN (:statuses) ORDER BY dateTaken DESC LIMIT :limit")
    suspend fun getPendingWithLimit(statuses: List<SyncStatus>, limit: Int): List<SyncStateEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(state: SyncStateEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(states: List<SyncStateEntity>)
    
    @Update
    suspend fun update(state: SyncStateEntity)
    
    @Query("UPDATE sync_state SET status = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: Long, status: SyncStatus, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE sync_state SET status = :status, lastError = :error, retryCount = retryCount + 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateFailed(id: Long, status: SyncStatus, error: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE sync_state SET status = :status, serverFileId = :serverFileId, serverEtag = :serverEtag, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateSynced(
        id: Long,
        status: SyncStatus,
        serverFileId: Long,
        serverEtag: String?,
        timestamp: Long = System.currentTimeMillis()
    )
    
    @Delete
    suspend fun delete(state: SyncStateEntity)
    
    @Query("DELETE FROM sync_state WHERE status = :status")
    suspend fun deleteByStatus(status: SyncStatus)
    
    @Query("DELETE FROM sync_state WHERE localPath = :path")
    suspend fun deleteByLocalPath(path: String)
}
