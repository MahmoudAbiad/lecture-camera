package com.mahmoud.lecturecamera.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * جلسة (محاضرة واحدة في يوم معيّن)
 */
@Entity(
    tableName = "sessions",
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
data class Session(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** معرّف المادة */
    val subjectId: Long,

    /** تسمية الجلسة (مثلاً "محاضرة 3 — 2026/11/08") */
    val label: String,

    /** تاريخ الجلسة (timestamp) */
    val date: Long,

    /** رقم المحاضرة التسلسلي */
    val sequenceNumber: Int,

    /** وقت الإنشاء */
    val createdAt: Long = System.currentTimeMillis()
)
