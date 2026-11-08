package com.mahmoud.lecturecamera.data

import android.content.Context

/**
 * إعدادات المسح — تُخزَّن في SharedPreferences
 */
class ScanPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** الطابع الزمني لآخر مسح ناجح (0 = لم يُجرَ مسح بعد) */
    var lastScanTimestamp: Long
        get() = prefs.getLong(KEY_LAST_SCAN, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_SCAN, value).apply()

    /** هامش الدقائق قبل بداية الحصة (للمطابقة) */
    var marginBeforeMinutes: Int
        get() = prefs.getInt(KEY_MARGIN_BEFORE, DEFAULT_MARGIN_MINUTES)
        set(value) = prefs.edit().putInt(KEY_MARGIN_BEFORE, value).apply()

    /** هامش الدقائق بعد نهاية الحصة (للمطابقة) */
    var marginAfterMinutes: Int
        get() = prefs.getInt(KEY_MARGIN_AFTER, DEFAULT_MARGIN_MINUTES)
        set(value) = prefs.edit().putInt(KEY_MARGIN_AFTER, value).apply()

    /** هل أتمّ المستخدم الإعداد الأولي (أدخل المواد والجدول)؟ */
    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING, value).apply()

    companion object {
        private const val PREFS_NAME = "scan_prefs"
        private const val KEY_LAST_SCAN = "last_scan_timestamp"
        private const val KEY_MARGIN_BEFORE = "margin_before_minutes"
        private const val KEY_MARGIN_AFTER = "margin_after_minutes"
        private const val KEY_ONBOARDING = "onboarding_completed"
        private const val DEFAULT_MARGIN_MINUTES = 10
    }
}
