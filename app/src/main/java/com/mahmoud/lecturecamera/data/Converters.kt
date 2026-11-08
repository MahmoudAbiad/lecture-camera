package com.mahmoud.lecturecamera.data

import androidx.room.TypeConverter
import com.mahmoud.lecturecamera.data.entity.PhotoStatus

/**
 * محوّلات الأنواع لقاعدة بيانات Room
 */
class Converters {
    @TypeConverter
    fun fromPhotoStatus(status: PhotoStatus): String = status.name

    @TypeConverter
    fun toPhotoStatus(value: String): PhotoStatus = PhotoStatus.valueOf(value)
}
