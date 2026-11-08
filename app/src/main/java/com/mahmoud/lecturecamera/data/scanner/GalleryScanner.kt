package com.mahmoud.lecturecamera.data.scanner

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.mahmoud.lecturecamera.data.ScanPreferences
import com.mahmoud.lecturecamera.data.entity.Photo
import com.mahmoud.lecturecamera.data.entity.PhotoStatus
import com.mahmoud.lecturecamera.data.entity.TimetableSlot
import com.mahmoud.lecturecamera.data.repository.LectureCameraRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class ScanResult(
    val totalScanned: Int,
    val matchedCount: Int,
    val newlyAddedCount: Int
)

/**
 * ماسح المعرض ومطابق الصور مع الجدول الأسبوعي
 */
class GalleryScanner(
    private val context: Context,
    private val repository: LectureCameraRepository,
    private val prefs: ScanPreferences
) {

    /**
     * فحص معرض الصور ومطابقتها مع الحصص
     * @param forceFullScan إذا كان true يعيد فحص كل الصور بغض النظر عن آخر مسح
     */
    suspend fun scanGallery(forceFullScan: Boolean = false): ScanResult = withContext(Dispatchers.IO) {
        val lastScan = if (forceFullScan) 0L else prefs.lastScanTimestamp
        val marginBefore = prefs.marginBeforeMinutes
        val marginAfter = prefs.marginAfterMinutes

        // جلب جميع الحصص الأسبوعية
        val allSlots = buildMap<Int, List<TimetableSlot>> {
            for (day in 1..7) {
                put(day, repository.getSlotsByDay(day))
            }
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED
        )

        val selection = if (lastScan > 0) {
            "${MediaStore.Images.Media.DATE_TAKEN} > ?"
        } else null

        val selectionArgs = if (lastScan > 0) {
            arrayOf(lastScan.toString())
        } else null

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} ASC"

        var totalScanned = 0
        var matchedCount = 0
        var newlyAddedCount = 0
        var maxTimestampFound = lastScan

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                totalScanned++
                val mediaId = cursor.getLong(idColumn)
                var dateTaken = cursor.getLong(dateTakenColumn)

                // إذا كان dateTaken صفر، نستخدم dateAdded (مضروباً بـ 1000 للتحويل لميللي ثانية)
                if (dateTaken <= 0L) {
                    val dateAddedSec = cursor.getLong(dateAddedColumn)
                    dateTaken = if (dateAddedSec > 0) dateAddedSec * 1000L else System.currentTimeMillis()
                }

                if (dateTaken > maxTimestampFound) {
                    maxTimestampFound = dateTaken
                }

                // التحقق من أن الصورة لم تُضف مسبقاً
                val existing = repository.getPhotoByMediaStoreId(mediaId)
                if (existing != null) {
                    continue
                }

                // تحويل التوقيت إلى يوم ودقيقة
                val dateTime = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(dateTaken),
                    ZoneId.systemDefault()
                )

                // تحويل يوم الأسبوع ليتوافق مع نظامنا: 1=الاثنين ... 7=الأحد
                val dayOfWeek = dateTime.dayOfWeek.value // 1..7
                val minuteOfDay = dateTime.hour * 60 + dateTime.minute

                // البحث عن حصة مطابقة ضمن نافذة الوقت
                val slotsForDay = allSlots[dayOfWeek].orEmpty()
                val matchedSlot = slotsForDay.firstOrNull { slot ->
                    val startWithMargin = slot.startMinuteOfDay - marginBefore
                    val endWithMargin = slot.endMinuteOfDay + marginAfter
                    minuteOfDay in startWithMargin..endWithMargin
                }

                if (matchedSlot != null) {
                    matchedCount++
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        mediaId
                    ).toString()

                    val newPhoto = Photo(
                        mediaStoreId = mediaId,
                        mediaStoreUri = contentUri,
                        subjectId = matchedSlot.subjectId,
                        status = PhotoStatus.PENDING,
                        capturedAt = dateTaken
                    )

                    val insertedId = repository.insertPhoto(newPhoto)
                    if (insertedId > 0) {
                        newlyAddedCount++
                    }
                }
            }
        }

        // تحديث وقت آخر مسح
        if (maxTimestampFound > prefs.lastScanTimestamp) {
            prefs.lastScanTimestamp = maxTimestampFound
        }

        ScanResult(
            totalScanned = totalScanned,
            matchedCount = matchedCount,
            newlyAddedCount = newlyAddedCount
        )
    }

    /**
     * إضافة صور تم اختيارها يدوياً من قِبل المستخدم (المسار البديل)
     */
    suspend fun addManualPhotos(uris: List<Uri>, defaultSubjectId: Long?): Int = withContext(Dispatchers.IO) {
        var added = 0
        for (uri in uris) {
            val mediaId = runCatching { ContentUris.parseId(uri) }.getOrDefault(uri.hashCode().toLong())
            val existing = repository.getPhotoByMediaStoreId(mediaId)
            if (existing != null) continue

            val photo = Photo(
                mediaStoreId = mediaId,
                mediaStoreUri = uri.toString(),
                subjectId = defaultSubjectId,
                status = if (defaultSubjectId != null) PhotoStatus.PENDING else PhotoStatus.REJECTED,
                capturedAt = System.currentTimeMillis()
            )
            if (repository.insertPhoto(photo) > 0) {
                added++
            }
        }
        added
    }

    /**
     * فحص صلاحية الصور المحفوظة والتحقق من أنها لم تُحذف من المعرض
     */
    suspend fun verifyExistingPhotos() = withContext(Dispatchers.IO) {
        val pendingPhotos = repository.getPendingPhotos()
        // يمكن التوسع في تنظيف الروابط التي حذفت
    }
}
