package com.mahmoud.lecturecamera.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoud.lecturecamera.data.AppDatabase
import com.mahmoud.lecturecamera.data.entity.Photo
import com.mahmoud.lecturecamera.data.entity.Session
import com.mahmoud.lecturecamera.data.entity.Subject
import com.mahmoud.lecturecamera.data.repository.LectureCameraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionItemWithPhotos(
    val session: Session,
    val photos: List<Photo>
)

class SubjectDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LectureCameraRepository(AppDatabase.getInstance(application))

    private val _currentSubjectId = MutableStateFlow<Long?>(null)

    val allSubjects: StateFlow<List<Subject>> = repository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uncategorizedPhotos: StateFlow<List<Photo>> = repository.getUncategorizedPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subject: StateFlow<Subject?> = _currentSubjectId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getAllSubjects().flatMapLatest { list ->
                flowOf(list.firstOrNull { it.id == id })
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sessionsWithPhotos: StateFlow<List<SessionItemWithPhotos>> = _currentSubjectId
        .flatMapLatest { subId ->
            if (subId == null) flowOf(emptyList())
            else repository.getSessionsBySubject(subId).flatMapLatest { sessionsList ->
                if (sessionsList.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    val flows: List<Flow<SessionItemWithPhotos>> = sessionsList.map { session ->
                        repository.getPhotosBySession(session.id).flatMapLatest { photos ->
                            flowOf(SessionItemWithPhotos(session, photos))
                        }
                    }
                    combine(flows) { it.toList() }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadSubject(subjectId: Long) {
        _currentSubjectId.value = subjectId
    }

    fun getPhotosForSession(sessionId: Long): Flow<List<Photo>> {
        return repository.getPhotosBySession(sessionId)
    }

    suspend fun getSession(sessionId: Long): Session? {
        return repository.getSession(sessionId)
    }

    fun updateSessionLabel(session: Session, newLabel: String) {
        viewModelScope.launch {
            repository.updateSession(session.copy(label = newLabel))
        }
    }

    fun deleteSession(session: Session) {
        viewModelScope.launch {
            repository.deleteSession(session)
        }
    }

    fun movePhotoToSession(photoId: Long, newSessionId: Long, newSubjectId: Long) {
        viewModelScope.launch {
            repository.assignPhotoToSession(photoId, newSessionId, newSubjectId)
        }
    }

    fun hidePhoto(photoId: Long) {
        viewModelScope.launch {
            repository.hidePhoto(photoId)
        }
    }

    fun assignUncategorizedPhoto(photoId: Long, subjectId: Long, sessionId: Long) {
        viewModelScope.launch {
            repository.assignPhotoToSession(photoId, sessionId, subjectId)
        }
    }
}
