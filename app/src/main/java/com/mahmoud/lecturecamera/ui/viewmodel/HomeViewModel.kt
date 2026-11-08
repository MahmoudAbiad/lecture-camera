package com.mahmoud.lecturecamera.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoud.lecturecamera.data.AppDatabase
import com.mahmoud.lecturecamera.data.ScanPreferences
import com.mahmoud.lecturecamera.data.entity.Subject
import com.mahmoud.lecturecamera.data.repository.LectureCameraRepository
import com.mahmoud.lecturecamera.data.scanner.GalleryScanner
import com.mahmoud.lecturecamera.data.scanner.ScanResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SubjectCardItem(
    val subject: Subject,
    val photoCount: Int,
    val coverUri: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LectureCameraRepository(AppDatabase.getInstance(application))
    private val prefs = ScanPreferences(application)
    private val scanner = GalleryScanner(application, repository, prefs)

    // حالة الفحص
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // نتائج ورسائل الفحص
    private val _scanMessage = MutableSharedFlow<String>()
    val scanMessage: SharedFlow<String> = _scanMessage.asSharedFlow()

    val pendingCount: StateFlow<Int> = repository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val uncategorizedCount: StateFlow<Int> = repository.getUncategorizedCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val subjectCards: StateFlow<List<SubjectCardItem>> = repository.getAllSubjects()
        .flatMapLatest { subjects ->
            if (subjects.isEmpty()) {
                flowOf(emptyList())
            } else {
                val flows: List<Flow<SubjectCardItem>> = subjects.map { subject ->
                    combine(
                        repository.getAcceptedCountBySubject(subject.id),
                        repository.getLatestPhotoUriForSubject(subject.id)
                    ) { count, latestUri ->
                        SubjectCardItem(
                            subject = subject,
                            photoCount = count,
                            coverUri = latestUri
                        )
                    }
                }
                combine(flows) { it.toList() }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * بدء فحص المعرض في الخلفية
     */
    fun startScan(forceFullScan: Boolean = false) {
        if (_isScanning.value) return
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val result: ScanResult = scanner.scanGallery(forceFullScan)
                val msg = if (result.newlyAddedCount > 0) {
                    "تم العثور على ${result.newlyAddedCount} صورة محاضرة جديدة مطابقة لجدولك!"
                } else if (result.totalScanned > 0) {
                    "تم فحص ${result.totalScanned} صورة، لا توجد صور جديدة تطابق وقت الحصص."
                } else {
                    "لا توجد صور جديدة في المعرض منذ آخر فحص."
                }
                _scanMessage.emit(msg)
            } catch (e: Exception) {
                _scanMessage.emit("حدث خطأ أثناء فحص الصور: ${e.localizedMessage ?: "خطأ غير معروف"}")
            } finally {
                _isScanning.value = false
            }
        }
    }

    /**
     * إضافة صور يدوياً بواسطة منتقي الصور
     */
    fun addManualPhotos(uris: List<Uri>, defaultSubjectId: Long? = null) {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                val added = scanner.addManualPhotos(uris, defaultSubjectId)
                _scanMessage.emit("تمت إضافة $added صورة بنجاح.")
            } catch (e: Exception) {
                _scanMessage.emit("فشلت إضافة الصور: ${e.localizedMessage}")
            } finally {
                _isScanning.value = false
            }
        }
    }
}
