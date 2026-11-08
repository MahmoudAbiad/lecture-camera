package com.mahmoud.lecturecamera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mahmoud.lecturecamera.data.ScanPreferences
import com.mahmoud.lecturecamera.ui.navigation.Screen
import com.mahmoud.lecturecamera.ui.screen.home.HomeScreen
import com.mahmoud.lecturecamera.ui.screen.setup.SubjectsSetupScreen
import com.mahmoud.lecturecamera.ui.screen.setup.TimetableSetupScreen
import com.mahmoud.lecturecamera.ui.theme.LectureCameraTheme
import com.mahmoud.lecturecamera.ui.viewmodel.SetupViewModel

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
                    val viewModel: SetupViewModel = viewModel()

                    NavHost(navController = navController, startDestination = startRoute) {

                        composable(Screen.SubjectsSetup.route) {
                            SubjectsSetupScreen(
                                viewModel = viewModel,
                                onNext = { navController.navigate(Screen.TimetableSetup.route) }
                            )
                        }

                        composable(Screen.TimetableSetup.route) {
                            TimetableSetupScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() },
                                onFinish = {
                                    viewModel.completeOnboarding()
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onEditSetup = {
                                    navController.navigate(Screen.SubjectsSetup.route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
