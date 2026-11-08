package com.mahmoud.lecturecamera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mahmoud.lecturecamera.data.ScanPreferences
import com.mahmoud.lecturecamera.ui.navigation.Screen
import com.mahmoud.lecturecamera.ui.screen.detail.SessionDetailScreen
import com.mahmoud.lecturecamera.ui.screen.detail.SubjectDetailScreen
import com.mahmoud.lecturecamera.ui.screen.detail.UncategorizedScreen
import com.mahmoud.lecturecamera.ui.screen.error.ErrorLogScreen
import com.mahmoud.lecturecamera.ui.screen.home.HomeScreen
import com.mahmoud.lecturecamera.ui.screen.review.ReviewPendingScreen
import com.mahmoud.lecturecamera.ui.screen.setup.SubjectsSetupScreen
import com.mahmoud.lecturecamera.ui.screen.setup.TimetableSetupScreen
import com.mahmoud.lecturecamera.ui.theme.LectureCameraTheme
import com.mahmoud.lecturecamera.ui.viewmodel.HomeViewModel
import com.mahmoud.lecturecamera.ui.viewmodel.ReviewViewModel
import com.mahmoud.lecturecamera.ui.viewmodel.SetupViewModel
import com.mahmoud.lecturecamera.ui.viewmodel.SubjectDetailViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = ScanPreferences(this)
        val startRoute =
            if (prefs.isOnboardingCompleted) Screen.Home.route
            else Screen.SubjectsSetup.route

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                LectureCameraTheme {
                    val navController = rememberNavController()
                    val setupViewModel: SetupViewModel = viewModel()
                    val homeViewModel: HomeViewModel = viewModel()
                    val reviewViewModel: ReviewViewModel = viewModel()
                    val detailViewModel: SubjectDetailViewModel = viewModel()

                    NavHost(navController = navController, startDestination = startRoute) {

                        composable(Screen.SubjectsSetup.route) {
                            SubjectsSetupScreen(
                                viewModel = setupViewModel,
                                onNext = { navController.navigate(Screen.TimetableSetup.route) }
                            )
                        }

                        composable(Screen.TimetableSetup.route) {
                            TimetableSetupScreen(
                                viewModel = setupViewModel,
                                onBack = { navController.popBackStack() },
                                onFinish = {
                                    setupViewModel.completeOnboarding()
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onEditSetup = {
                                    navController.navigate(Screen.SubjectsSetup.route)
                                },
                                onOpenErrorLog = {
                                    navController.navigate(Screen.ErrorLog.route)
                                },
                                onOpenPendingReview = {
                                    navController.navigate(Screen.ReviewPending.route)
                                },
                                onOpenSubject = { subjectId ->
                                    navController.navigate(Screen.SubjectDetail.createRoute(subjectId))
                                },
                                onOpenUncategorized = {
                                    navController.navigate(Screen.Uncategorized.route)
                                }
                            )
                        }

                        composable(Screen.ReviewPending.route) {
                            ReviewPendingScreen(
                                viewModel = reviewViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.ErrorLog.route) {
                            ErrorLogScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Screen.SubjectDetail.route,
                            arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val subjectId = backStackEntry.arguments?.getLong("subjectId") ?: 0L
                            SubjectDetailScreen(
                                subjectId = subjectId,
                                viewModel = detailViewModel,
                                onBack = { navController.popBackStack() },
                                onOpenSession = { sessionId ->
                                    navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                                }
                            )
                        }

                        composable(
                            route = Screen.SessionDetail.route,
                            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: 0L
                            SessionDetailScreen(
                                sessionId = sessionId,
                                viewModel = detailViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Screen.Uncategorized.route) {
                            UncategorizedScreen(
                                viewModel = detailViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
