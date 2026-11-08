package com.mahmoud.lecturecamera.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoud.lecturecamera.data.AppDatabase
import com.mahmoud.lecturecamera.data.entity.Photo
import com.mahmoud.lecturecamera.data.entity.Session
import com.mahmoud.lecturecamera.data.entity.Subject
import com.mahmoud.lecturecamera.data.repository.LectureCameraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class PendingGroup(
    val subject: Subject,
    val date: LocalDate,
    val photos: List<Photo>
)

class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LectureCameraRepository(AppDatabase.getInstance(application))

    val subjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // الصور المحددة للقبول (افتراضياً كل الصور معلّمة)
    private val _selectedPhotoIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedPhotoIds: StateFlow<Set<Long>> = _selectedPhotoIds.asStateFlow()

    // تجميع الصور المقترحة حسب المادة وتاريخ الالتقاط
    val groupedPending: StateFlow<List<PendingGroup>> = combine(
        repository.getPendingPhotos(),
        repository.getAllSubjects()
    ) { photos, subjectsList ->
        val subjectMap = subjectsList.associateBy { it.id }

        // تهيئة التحديد الافتراضي ليشمل كل الصور الجديدة
        if (_selectedPhotoIds.value.isEmpty() && photos.isNotEmpty()) {
            _selectedPhotoIds.value = photos.map { it.id }.toSet()
        }

        photos.groupBy { photo ->
            val date = LocalDateTimeFromMillis(photo.capturedAt)
            Pair(photo.subjectId, date)
        }.mapNotNull { (key, groupPhotos) ->
            val (subjectId, date) = key
            val subject = subjectId?.let { subjectMap[it] } ?: subjectsList.firstOrNull()
            if (subject != null) {
                PendingGroup(subject = subject, date = date, photos = groupPhotos)
            } else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePhotoSelection(photoId: Long) {
        val current = _selectedPhotoIds.value.toMutableSet()
        if (current.contains(photoId)) {
            current.remove(photoId)
        } else {
            current.add(photoId)
        }
        _selectedPhotoIds.value = current
    }

    fun selectAllInGroup(photos: List<Photo>) {
        val current = _selectedPhotoIds.value.toMutableSet()
        current.addAll(photos.map { it.id })
        _selectedPhotoIds.value = current
    }

    fun deselectAllInGroup(photos: List<Photo>) {
        val current = _selectedPhotoIds.value.toMutableSet()
        current.removeAll(photos.map { it.id }.toSet())
        _selectedPhotoIds.value = current
    }

    /**
     * قبول مجموعة صور وإنشاء جلسة تلقائية لها باسم ورقم المحاضرة
     */
    fun acceptGroup(group: PendingGroup) {
        viewModelScope.launch {
            val selectedInGroup = group.photos.filter { _selectedPhotoIds.value.contains(it.id) }
            val unselectedInGroup = group.photos.filterNot { _selectedPhotoIds.value.contains(it.id) }

            if (selectedInGroup.isNotEmpty()) {
                val nextSeq = repository.getNextSequenceNumber(group.subject.id)
                val dateStr = group.date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                val sessionLabel = "محاضرة $nextSeq \u2014 $dateStr"

                val epochMillis = group.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val sessionId = repository.insertSession(
                    Session(
                        subjectId = group.subject.id,
                        label = sessionLabel,
                        date = epochMillis,
                        sequenceNumber = nextSeq
                    )
                )

                for (photo in selectedInGroup) {
                    repository.assignPhotoToSession(photo.id, sessionId, group.subject.id)
                }
            }

            // الصور غير المحددة في المجموعة تُنقل إلى غير مصنف (REJECTED)
            for (photo in unselectedInGroup) {
                repository.rejectPhoto(photo.id)
            }
        }
    }

    /**
     * رفض صورة ونقلها مباشرة إلى ألبوم "غير مصنف"
     */
    fun rejectPhoto(photoId: Long) {
        viewModelScope.launch {
            repository.rejectPhoto(photoId)
        }
    }

    /**
     * تغيير المادة المقترحة لمجموعة صور
     */
    fun changeGroupSubject(photos: List<Photo>, newSubjectId: Long) {
        viewModelScope.launch {
            for (photo in photos) {
                repository.updatePhoto(photo.copy(subjectId = newSubjectId))
            }
        }
    }

    private fun LocalDateTimeFromMillis(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    }
}
