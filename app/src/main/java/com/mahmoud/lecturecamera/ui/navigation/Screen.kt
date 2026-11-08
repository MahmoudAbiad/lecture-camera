package com.mahmoud.lecturecamera.ui.navigation

sealed class Screen(val route: String) {
    data object SubjectsSetup : Screen("subjects_setup")
    data object TimetableSetup : Screen("timetable_setup")
    data object Home : Screen("home")
    data object ReviewPending : Screen("review_pending")
    data object Uncategorized : Screen("uncategorized")

    data object SubjectDetail : Screen("subject_detail/{subjectId}") {
        fun createRoute(subjectId: Long) = "subject_detail/$subjectId"
    }

    data object SessionDetail : Screen("session_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "session_detail/$sessionId"
    }
}
