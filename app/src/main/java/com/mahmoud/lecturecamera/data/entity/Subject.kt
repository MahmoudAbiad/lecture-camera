package com.mahmoud.lecturecamera.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * المادة الدراسية (مثلاً: رياضيات، فيزياء)
 */
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** اسم المادة */
    val name: String,

    /** لون مميز بصيغة HEX (مثلاً #2E7D32) */
    val colorHex: String,

    /** وقت الإنشاء */
    val createdAt: Long = System.currentTimeMillis()
)
