package com.mahmoud.lecturecamera.util

import android.content.Context
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * مسجل الأخطاء والاستثناءات غير الملتقطة لمشاركتها ونسخها بسهولة
 */
object ErrorLogger {

    private const val PREFS_NAME = "error_logs"
    private const val KEY_LAST_ERROR = "last_error_report"
    private const val KEY_LAST_ERROR_TIME = "last_error_time"

    fun init(context: Context) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            logError(context, throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    fun logError(context: Context, throwable: Throwable) {
        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val stackTrace = sw.toString()

        val timeFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val report = buildString {
            append("=== تقرير خطأ lecture-camera ===\n")
            append("التاريخ: $timeFormatted\n")
            append("نوع الخطأ: ${throwable.javaClass.simpleName}\n")
            append("الرسالة: ${throwable.localizedMessage ?: "لا توجد رسالة"}\n")
            append("----------------------------------------\n")
            append(stackTrace)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LAST_ERROR, report)
            .putLong(KEY_LAST_ERROR_TIME, System.currentTimeMillis())
            .apply()
    }

    fun getLastError(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_ERROR, null)
    }

    fun clearError(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
