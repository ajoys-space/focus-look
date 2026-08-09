package com.focuslock.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.focuslock.app.data.model.ThemeMode
import com.focuslock.app.ui.navigation.FocusLockNavGraph
import com.focuslock.app.ui.screens.settings.SettingsViewModel
import com.focuslock.app.ui.theme.FocusLockTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Requests high refresh rate (120Hz) on supported devices for buttery smooth animations
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val params = window.attributes
            params.preferredRefreshRate = 120f
            window.attributes = params
        }
        
        // Remove window background to reduce overdraw
        window.setBackgroundDrawable(null)
        
        enableEdgeToEdge()
        setContent {
            // Reuses SettingsViewModel purely to read the persisted theme
            // preference at the app root — keeps MainActivity itself free
            // of direct Repository/DataStore access.
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val uiState by settingsViewModel.uiState.collectAsState()

            FocusLockTheme(
                darkTheme = when (uiState.preferences.themeMode) {
                    ThemeMode.DARK -> true
                    ThemeMode.LIGHT -> false
                    ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                },
                dynamicColor = uiState.preferences.themeMode == ThemeMode.SYSTEM
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FocusLockNavGraph()
                }
            }
        }
    }
}