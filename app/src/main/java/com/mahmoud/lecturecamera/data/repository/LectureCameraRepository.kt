package com.mahmoud.lecturecamera.data.repository

import com.mahmoud.lecturecamera.data.AppDatabase
import com.mahmoud.lecturecamera.data.entity.Photo
import com.mahmoud.lecturecamera.data.entity.Session
import com.mahmoud.lecturecamera.data.entity.Subject
import com.mahmoud.lecturecamera.data.entity.TimetableSlot

/**
 * مستودع بيانات موحّد — الواجهة الوحيدة بين طبقة المنطق وقاعدة البيانات
 */
class LectureCameraRepository(private val db: AppDatabase) {

    // ── المواد ──────────────────────────────────────────

    fun getAllSubjects() = db.subjectDao().getAll()

    suspend fun getSubject(id: Long) = db.subjectDao().getById(id)

    suspend fun insertSubject(subject: Subject) = db.subjectDao().insert(subject)

    suspend fun updateSubject(subject: Subject) = db.subjectDao().update(subject)

    suspend fun deleteSubject(subject: Subject) = db.subjectDao().delete(subject)

    fun getSubjectCount() = db.subjectDao().getCount()

    // ── حصص الجدول ──────────────────────────────────────

    fun getAllSlots() = db.timetableSlotDao().getAll()

    fun getSlotsBySubject(subjectId: Long) = db.timetableSlotDao().getBySubject(subjectId)

    suspend fun getSlotsByDay(dayOfWeek: Int) = db.timetableSlotDao().getByDay(dayOfWeek)

    suspend fun insertSlot(slot: TimetableSlot) = db.timetableSlotDao().insert(slot)

    suspend fun updateSlot(slot: TimetableSlot) = db.timetableSlotDao().update(slot)

    suspend fun deleteSlot(slot: TimetableSlot) = db.timetableSlotDao().delete(slot)

    // ── الجلسات ─────────────────────────────────────────

    fun getSessionsBySubject(subjectId: Long) = db.sessionDao().getBySubject(subjectId)

    suspend fun getSession(id: Long) = db.sessionDao().getById(id)

    suspend fun getNextSequenceNumber(subjectId: Long): Int {
        return (db.sessionDao().getMaxSequenceNumber(subjectId) ?: 0) + 1
    }

    suspend fun insertSession(session: Session) = db.sessionDao().insert(session)

    suspend fun updateSession(session: Session) = db.sessionDao().update(session)

    suspend fun deleteSession(session: Session) = db.sessionDao().delete(session)

    // ── الصور ────────────────────────────────────────────

    fun getPhotosBySession(sessionId: Long) = db.photoDao().getBySession(sessionId)

    fun getPhotosBySubject(subjectId: Long) = db.photoDao().getBySubject(subjectId)

    fun getPendingPhotos() = db.photoDao().getPending()

    fun getUncategorizedPhotos() = db.photoDao().getUncategorized()

    fun getPendingCount() = db.photoDao().getPendingCount()

    fun getAcceptedCountBySubject(subjectId: Long) =
        db.photoDao().getAcceptedCountBySubject(subjectId)

    suspend fun getPhotoByMediaStoreId(mediaStoreId: Long) =
        db.photoDao().getByMediaStoreId(mediaStoreId)

    suspend fun insertPhoto(photo: Photo) = db.photoDao().insert(photo)

    suspend fun insertPhotos(photos: List<Photo>) = db.photoDao().insertAll(photos)

    suspend fun updatePhoto(photo: Photo) = db.photoDao().update(photo)

    suspend fun assignPhotoToSession(photoId: Long, sessionId: Long, subjectId: Long) =
        db.photoDao().assignToSession(photoId, sessionId, subjectId)

    suspend fun rejectPhoto(photoId: Long) = db.photoDao().reject(photoId)

    suspend fun hidePhoto(photoId: Long) = db.photoDao().hide(photoId)
}
