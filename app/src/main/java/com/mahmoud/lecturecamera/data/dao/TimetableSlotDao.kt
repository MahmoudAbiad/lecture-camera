package com.mahmoud.lecturecamera.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mahmoud.lecturecamera.data.entity.TimetableSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableSlotDao {

    @Query("SELECT * FROM timetable_slots ORDER BY dayOfWeek, startMinuteOfDay")
    fun getAll(): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable_slots WHERE subjectId = :subjectId")
    fun getBySubject(subjectId: Long): Flow<List<TimetableSlot>>

    @Query("SELECT * FROM timetable_slots WHERE dayOfWeek = :dayOfWeek ORDER BY startMinuteOfDay")
    suspend fun getByDay(dayOfWeek: Int): List<TimetableSlot>

    @Insert
    suspend fun insert(slot: TimetableSlot): Long

    @Update
    suspend fun update(slot: TimetableSlot)

    @Delete
    suspend fun delete(slot: TimetableSlot)
}
