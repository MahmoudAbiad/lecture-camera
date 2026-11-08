package com.mahmoud.lecturecamera.data.entity

/**
 * حالات الصورة في التطبيق
 */
enum class PhotoStatus {
    /** مقترحة، تنتظر مراجعة المستخدم */
    PENDING,

    /** مقبولة ومصنّفة في جلسة */
    ACCEPTED,

    /** مرفوضة — تظهر في ألبوم "غير مصنف" */
    REJECTED,

    /** مخفية من التطبيق (لا تُحذف من الهاتف) */
    HIDDEN
}
