package com.mahmoud.lecturecamera.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mahmoud.lecturecamera.data.entity.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE subjectId = :subjectId ORDER BY date DESC")
    fun getBySubject(subjectId: Long): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getById(id: Long): Session?

    @Query("SELECT MAX(sequenceNumber) FROM sessions WHERE subjectId = :subjectId")
    suspend fun getMaxSequenceNumber(subjectId: Long): Int?

    @Insert
    suspend fun insert(session: Session): Long

    @Update
    suspend fun update(session: Session)

    @Delete
    suspend fun delete(session: Session)
}
