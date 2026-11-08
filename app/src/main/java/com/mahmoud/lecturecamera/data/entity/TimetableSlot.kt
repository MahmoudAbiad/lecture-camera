package com.mahmoud.lecturecamera.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * حصة في الجدول الأسبوعي
 *
 * dayOfWeek يتبع ISO-8601: الاثنين=1 .. الأحد=7
 * الأوقات تُخزَّن بالدقائق من بداية اليوم (مثلاً 480 = 08:00)
 */
@Entity(
    tableName = "timetable_slots",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class TimetableSlot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** معرّف المادة */
    val subjectId: Long,

    /** يوم الأسبوع (1=الاثنين .. 7=الأحد) */
    val dayOfWeek: Int,

    /** وقت البداية بالدقائق من منتصف الليل */
    val startMinuteOfDay: Int,

    /** وقت النهاية بالدقائق من منتصف الليل */
    val endMinuteOfDay: Int
)
