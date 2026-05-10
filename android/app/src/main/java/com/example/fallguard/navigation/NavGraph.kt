package com.example.fallguard.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fallguard.ui.screens.*

object Routes {
    const val WELCOME       = "welcome"
    const val PERMISSIONS   = "permissions"
    const val PROFILE_SETUP = "profile_setup"
    const val HOME          = "home"
    const val COUNTDOWN     = "countdown"
    const val ALERT_SENT    = "alert_sent"
    const val CONTACT_ALERT = "contact_alert"
    const val USER_CHECK_IN = "user_check_in"
    const val RESOLVED      = "resolved"
    const val SETTINGS      = "settings"
}

@Composable
fun FallGuardNavGraph(
    factory: ViewModelProvider.Factory,
    startOnboarding: Boolean = true
) {
    val navController = rememberNavController()
    val start = if (startOnboarding) Routes.WELCOME else Routes.HOME

    NavHost(navController = navController, startDestination = start) {

        composable(Routes.WELCOME) {
            WelcomeScreen(onContinue = { navController.navigate(Routes.PERMISSIONS) })
        }

        composable(Routes.PERMISSIONS) {
            PermissionsScreen(onContinue = { navController.navigate(Routes.PROFILE_SETUP) })
        }

        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(
                vmFactory = factory,
                onComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                vmFactory = factory,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onSimulateFall = { navController.navigate(Routes.COUNTDOWN) }
            )
        }

        composable(Routes.COUNTDOWN) {
            FallCountdownScreen(
                vmFactory = factory,
                onCancel   = { navController.navigate(Routes.RESOLVED) },
                onAlertSent = { navController.navigate(Routes.ALERT_SENT) }
            )
        }

        composable(Routes.ALERT_SENT) {
            AlertSentScreen(
                onCancel = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CONTACT_ALERT) {
            ContactAlertScreen(
                onRespond = { navController.navigate(Routes.USER_CHECK_IN) },
                onCancel  = { navController.popBackStack() }
            )
        }

        composable(Routes.USER_CHECK_IN) {
            UserCheckInScreen(
                onResolved = { navController.navigate(Routes.RESOLVED) }
            )
        }

        composable(Routes.RESOLVED) {
            ResolvedScreen(onBackHome = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
            })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                vmFactory = factory,
                onBack = { navController.popBackStack() },
                onReset = {
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
