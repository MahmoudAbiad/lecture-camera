package com.mahmoud.lecturecamera.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mahmoud.lecturecamera.data.dao.PhotoDao
import com.mahmoud.lecturecamera.data.dao.SessionDao
import com.mahmoud.lecturecamera.data.dao.SubjectDao
import com.mahmoud.lecturecamera.data.dao.TimetableSlotDao
import com.mahmoud.lecturecamera.data.entity.Photo
import com.mahmoud.lecturecamera.data.entity.Session
import com.mahmoud.lecturecamera.data.entity.Subject
import com.mahmoud.lecturecamera.data.entity.TimetableSlot

@Database(
    entities = [
        Subject::class,
        TimetableSlot::class,
        Session::class,
        Photo::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun timetableSlotDao(): TimetableSlotDao
    abstract fun sessionDao(): SessionDao
    abstract fun photoDao(): PhotoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lecture_camera.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
