package com.focuslock.app.ui.screens.allusage

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslock.app.data.local.UsageStatsHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class DetailedAppUsage(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap?,
    val usageMillis: Long
)

data class AllAppsUsageUiState(
    val appsUsage: List<DetailedAppUsage> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AllAppsUsageViewModel @Inject constructor(
    private val usageStatsHelper: UsageStatsHelper,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AllAppsUsageUiState())
    val uiState: StateFlow<AllAppsUsageUiState> = _uiState.asStateFlow()
    
    private val iconCache = mutableMapOf<String, ImageBitmap>()

    init {
        loadUsageData()
    }

    fun loadUsageData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val apps = withContext(Dispatchers.Default) {
                val sessions = usageStatsHelper.getTodaysSessions()
                val pm = getApplication<Application>().packageManager
                
                sessions.groupBy { it.packageName }
                    .map { (packageName, pkgSessions) ->
                        val totalMillis = pkgSessions.sumOf { it.durationMillis }
                        val appLabel = try {
                            val info = pm.getApplicationInfo(packageName, 0)
                            pm.getApplicationLabel(info).toString()
                        } catch (e: Exception) {
                            packageName
                        }
                        val appIcon = iconCache[packageName] ?: try {
                            val bitmap = pm.getApplicationIcon(packageName).toBitmap().asImageBitmap()
                            iconCache[packageName] = bitmap
                            bitmap
                        } catch (e: Exception) {
                            null
                        }
                        
                        DetailedAppUsage(
                            packageName = packageName,
                            appName = appLabel,
                            icon = appIcon,
                            usageMillis = totalMillis
                        )
                    }
                    .filter { it.usageMillis > 0 }
                    .sortedByDescending { it.usageMillis }
            }
            
            _uiState.value = AllAppsUsageUiState(
                appsUsage = apps,
                isLoading = false
            )
        }
    }
}
