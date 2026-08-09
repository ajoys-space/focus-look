package com.focuslock.app.ui.screens.appselection

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.focuslock.app.data.local.InstalledApp
import com.focuslock.app.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    onOpenLimitSettings: (String) -> Unit,
    viewModel: AppSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchFieldValue by remember { mutableStateOf("") }

    if (uiState.pendingAppForLimit != null) {
        SetLimitDialog(
            appName = uiState.pendingAppForLimit!!.appName,
            onConfirm = { minutes -> viewModel.setLimitForPendingApp(minutes) },
            onDismiss = { viewModel.cancelPendingLimit() }
        )
    }

    // For staggered list entry animation
    val visibleState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchFieldValue,
                        onValueChange = {
                            searchFieldValue = it
                            viewModel.onSearchQueryChanged(it)
                        },
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        placeholder = { Text("Search apps...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchFieldValue.isNotEmpty()) {
                                IconButton(onClick = { 
                                    searchFieldValue = ""
                                    viewModel.onSearchQueryChanged("")
                                }) {
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            disabledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        ),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { viewModel.saveSelection(onDone) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Save Changes (${uiState.selectedPackages.size} selected)", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.filteredApps, key = { it.packageName }) { app ->
                        AnimatedVisibility(
                            visibleState = visibleState,
                            enter = slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 } + fadeIn()
                        ) {
                            AppRow(
                                app = app,
                                isSelected = app.packageName in uiState.selectedPackages,
                                isAlreadyLocked = app.packageName in uiState.alreadyLockedPackages,
                                onToggle = { viewModel.toggleAppSelected(app.packageName) },
                                onOpenLimitSettings = { onOpenLimitSettings(app.packageName) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    isSelected: Boolean,
    isAlreadyLocked: Boolean,
    onToggle: () -> Unit,
    onOpenLimitSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.fillMaxWidth(),
        headlineContent = { Text(app.appName, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(app.packageName, style = MaterialTheme.typography.bodySmall) },
        leadingContent = {
            Surface(
                shape = MaterialTheme.shapes.small,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp
            ) {
                Image(
                    bitmap = app.icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isSelected && isAlreadyLocked) {
                    TextButton(onClick = onOpenLimitSettings) {
                        Text("Limit")
                    }
                }
                Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
            }
        }
    )
}

@Composable
private fun SetLimitDialog(
    appName: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(60) }
    var customInput by remember { mutableStateOf("") }
    var isCustom by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Limit for $appName") },
        text = {
            Column {
                Constants.DEFAULT_LIMIT_OPTIONS_MINUTES.forEach { minutes ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .selectable(
                                selected = !isCustom && selectedMinutes == minutes,
                                onClick = {
                                    isCustom = false
                                    selectedMinutes = minutes
                                }
                            )
                    ) {
                        RadioButton(selected = !isCustom && selectedMinutes == minutes, onClick = null)
                        Spacer(Modifier.width(12.dp))
                        Text(if (minutes < 60) "$minutes minutes" else "${minutes / 60} hour(s)")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .selectable(
                            selected = isCustom,
                            onClick = { isCustom = true }
                        )
                ) {
                    RadioButton(selected = isCustom, onClick = null)
                    Spacer(Modifier.width(12.dp))
                    Text("Custom")
                }

                if (isCustom) {
                    OutlinedTextField(
                        value = customInput,
                        onValueChange = { customInput = it },
                        label = { Text("Minutes") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val finalMinutes = if (isCustom) customInput.toIntOrNull() ?: 60 else selectedMinutes
                onConfirm(finalMinutes)
            }) {
                Text("Lock App")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
