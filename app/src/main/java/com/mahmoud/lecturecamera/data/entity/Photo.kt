package com.mahmoud.lecturecamera.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * صورة من معرض الهاتف مرتبطة بالتطبيق.
 * لا تُنسخ الصورة — يُحفظ معرّف MediaStore فقط.
 */
@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("sessionId"),
        Index("subjectId"),
        Index(value = ["mediaStoreId"], unique = true)
    ]
)
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** معرّف الصورة في MediaStore */
    val mediaStoreId: Long,

    /** عنوان URI في MediaStore */
    val mediaStoreUri: String,

    /** معرّف الجلسة (فارغ إن لم تُصنَّف بعد) */
    val sessionId: Long? = null,

    /** معرّف المادة (فارغ إن لم تُصنَّف بعد) */
    val subjectId: Long? = null,

    /** حالة الصورة */
    val status: PhotoStatus = PhotoStatus.PENDING,

    /** وقت التقاط الصورة */
    val capturedAt: Long,

    /** وقت إضافتها إلى التطبيق */
    val addedAt: Long = System.currentTimeMillis()
)
