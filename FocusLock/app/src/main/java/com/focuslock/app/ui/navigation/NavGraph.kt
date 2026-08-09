package com.focuslock.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.focuslock.app.service.UsageTrackingForegroundService
import com.focuslock.app.ui.screens.appselection.AppSelectionScreen
import com.focuslock.app.ui.screens.appselection.SetLimitScreen
import com.focuslock.app.ui.screens.home.HomeScreen
import com.focuslock.app.ui.screens.onboarding.OnboardingScreen
import com.focuslock.app.ui.screens.permissions.PermissionsScreen
import com.focuslock.app.ui.screens.settings.SettingsScreen
import com.focuslock.app.ui.screens.splash.SplashScreen
import com.focuslock.app.ui.screens.statistics.StatisticsScreen
import com.focuslock.app.ui.screens.allusage.AllAppsUsageScreen

private const val ANIM_DURATION_MS = 300

@Composable
fun FocusLockNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION_MS)) + 
            slideInHorizontally(animationSpec = tween(ANIM_DURATION_MS)) { it / 3 }
        },
        exitTransition = {
            fadeOut(animationSpec = tween(ANIM_DURATION_MS)) + 
            slideOutHorizontally(animationSpec = tween(ANIM_DURATION_MS)) { -it / 3 }
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(ANIM_DURATION_MS)) + 
            slideInHorizontally(animationSpec = tween(ANIM_DURATION_MS)) { -it / 3 }
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(ANIM_DURATION_MS)) + 
            slideOutHorizontally(animationSpec = tween(ANIM_DURATION_MS)) { it / 3 }
        }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateNext = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.PERMISSIONS) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PERMISSIONS) {
            val context = LocalContext.current
            PermissionsScreen(
                onAllPermissionsGranted = {
                    UsageTrackingForegroundService.start(context)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PERMISSIONS) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToAppSelection = { navController.navigate(Routes.APP_SELECTION) },
                onNavigateToStatistics = { navController.navigate(Routes.STATISTICS) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToPermissions = { navController.navigate(Routes.PERMISSIONS) },
                onNavigateToAllAppsUsage = { navController.navigate(Routes.ALL_APPS_USAGE) }
            )
        }

        composable(Routes.APP_SELECTION) {
            AppSelectionScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() },
                onOpenLimitSettings = { packageName ->
                    navController.navigate(Routes.setLimitRoute(packageName))
                }
            )
        }

        composable(
            route = Routes.SET_LIMIT,
            arguments = listOf(navArgument("packageName") { type = NavType.StringType })
        ) {
            SetLimitScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(Routes.STATISTICS) {
            StatisticsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ALL_APPS_USAGE) {
            AllAppsUsageScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}