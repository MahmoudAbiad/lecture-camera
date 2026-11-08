package com.mahmoud.lecturecamera.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mahmoud.lecturecamera.data.entity.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAll(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getById(id: Long): Subject?

    @Insert
    suspend fun insert(subject: Subject): Long

    @Update
    suspend fun update(subject: Subject)

    @Delete
    suspend fun delete(subject: Subject)

    @Query("SELECT COUNT(*) FROM subjects")
    fun getCount(): Flow<Int>
}
