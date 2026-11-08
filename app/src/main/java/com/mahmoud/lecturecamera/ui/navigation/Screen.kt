package com.mahmoud.lecturecamera.ui.navigation

sealed class Screen(val route: String) {
    data object SubjectsSetup : Screen("subjects_setup")
    data object TimetableSetup : Screen("timetable_setup")
    data object Home : Screen("home")
    data object ReviewPending : Screen("review_pending")
}
