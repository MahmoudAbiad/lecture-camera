package com.mahmoud.lecturecamera.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahmoud.lecturecamera.data.AppDatabase
import com.mahmoud.lecturecamera.data.ScanPreferences
import com.mahmoud.lecturecamera.data.entity.Subject
import com.mahmoud.lecturecamera.data.entity.TimetableSlot
import com.mahmoud.lecturecamera.data.repository.LectureCameraRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SetupViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LectureCameraRepository(AppDatabase.getInstance(application))
    private val prefs = ScanPreferences(application)

    val subjects = repository.getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val slots = repository.getAllSlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSubject(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertSubject(Subject(name = name, colorHex = colorHex))
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch { repository.updateSubject(subject) }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch { repository.deleteSubject(subject) }
    }

    fun addSlot(subjectId: Long, dayOfWeek: Int, startMinute: Int, endMinute: Int) {
        viewModelScope.launch {
            repository.insertSlot(
                TimetableSlot(
                    subjectId = subjectId,
                    dayOfWeek = dayOfWeek,
                    startMinuteOfDay = startMinute,
                    endMinuteOfDay = endMinute
                )
            )
        }
    }

    fun deleteSlot(slot: TimetableSlot) {
        viewModelScope.launch { repository.deleteSlot(slot) }
    }

    fun completeOnboarding() {
        prefs.isOnboardingCompleted = true
    }
}
