package com.mahmoud.lecturecamera.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoud.lecturecamera.data.AppDatabase
import com.mahmoud.lecturecamera.data.entity.Subject
import com.mahmoud.lecturecamera.data.repository.LectureCameraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class SubjectCardItem(
    val subject: Subject,
    val photoCount: Int,
    val coverUri: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LectureCameraRepository(AppDatabase.getInstance(application))

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
}
