package com.mahmoud.lecturecamera.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mahmoud.lecturecamera.data.entity.Photo
import com.mahmoud.lecturecamera.data.entity.PhotoStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photos WHERE sessionId = :sessionId ORDER BY capturedAt ASC")
    fun getBySession(sessionId: Long): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE subjectId = :subjectId ORDER BY capturedAt DESC")
    fun getBySubject(subjectId: Long): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE status = 'PENDING' ORDER BY capturedAt DESC")
    fun getPending(): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE status = 'REJECTED' ORDER BY capturedAt DESC")
    fun getUncategorized(): Flow<List<Photo>>

    @Query("SELECT COUNT(*) FROM photos WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM photos WHERE subjectId = :subjectId AND status = 'ACCEPTED'")
    fun getAcceptedCountBySubject(subjectId: Long): Flow<Int>

    @Query("SELECT * FROM photos WHERE mediaStoreId = :mediaStoreId LIMIT 1")
    suspend fun getByMediaStoreId(mediaStoreId: Long): Photo?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photo: Photo): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(photos: List<Photo>): List<Long>

    @Update
    suspend fun update(photo: Photo)

    @Query("UPDATE photos SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: PhotoStatus)

    @Query("UPDATE photos SET sessionId = :sessionId, subjectId = :subjectId, status = 'ACCEPTED' WHERE id = :id")
    suspend fun assignToSession(id: Long, sessionId: Long, subjectId: Long)

    @Query("UPDATE photos SET status = 'REJECTED', sessionId = NULL WHERE id = :id")
    suspend fun reject(id: Long)

    @Query("UPDATE photos SET status = 'HIDDEN' WHERE id = :id")
    suspend fun hide(id: Long)

    @Delete
    suspend fun delete(photo: Photo)
}
