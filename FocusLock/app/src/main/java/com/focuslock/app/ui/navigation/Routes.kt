package com.focuslock.app.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val PERMISSIONS = "permissions"
    const val HOME = "home"
    const val APP_SELECTION = "app_selection"
    const val SET_LIMIT = "set_limit/{packageName}"
    const val BLOCKING_OVERLAY = "blocking_overlay"
    const val CHALLENGE = "challenge"
    const val STATISTICS = "statistics"
    const val ALL_APPS_USAGE = "all_apps_usage"
    const val SETTINGS = "settings"

    fun setLimitRoute(packageName: String) = "set_limit/$packageName"
}