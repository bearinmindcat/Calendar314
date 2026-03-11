package com.yourname.challengetracker.MainScreens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    isDarkMode: Boolean = false,
    onThemeChange: (Boolean) -> Unit = {},
    onClearData: () -> Unit = {},
    onExportData: (String) -> Unit = {},
    onImportData: (String) -> Boolean = { false }
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showImportSuccessDialog by remember { mutableStateOf(false) }
    var showImportErrorDialog by remember { mutableStateOf(false) }
    var showPlannedFeaturesDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Export launcher - creates a new document
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportData(uri.toString())
        }
    }

    // Import launcher - opens an existing document
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val json = inputStream?.bufferedReader().use { it?.readText() } ?: ""
                if (onImportData(json)) {
                    showImportSuccessDialog = true
                } else {
                    showImportErrorDialog = true
                }
            } catch (e: Exception) {
                showImportErrorDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            // Data Section
            SettingsSection(title = "Data") {
                SettingsClickableItem(
                    title = "Export Data",
                    subtitle = "Save your data to a file",
                    onClick = {
                        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
                        exportLauncher.launch("challenge_tracker_backup_$timestamp.json")
                    }
                )

                SettingsClickableItem(
                    title = "Import Data",
                    subtitle = "Restore data from a file",
                    onClick = {
                        importLauncher.launch("application/json")
                    }
                )

                SettingsClickableItem(
                    title = "Clear All Data",
                    subtitle = "Delete all calendar and journal entries",
                    onClick = { showClearDataDialog = true },
                    textColor = Color(0xFFEF5350)
                )
            }

            Divider(color = Color.Gray.copy(alpha = 0.2f))

            // About Section
            SettingsSection(title = "About") {
                SettingsClickableItem(
                    title = "Version",
                    subtitle = "1.0.1",
                    onClick = { }
                )

                SettingsClickableItem(
                    title = "About",
                    subtitle = "Learn more about this app",
                    onClick = { showAboutDialog = true }
                )

                SettingsClickableItem(
                    title = "Planned Features",
                    subtitle = "See what's coming next",
                    onClick = { showPlannedFeaturesDialog = true }
                )
            }
        }
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About calendar314") },
            text = {
                Text("A simple FOSS app with a calendar to track events and personal goals with color date indexing & journal entries.\n\nVersion: 1.0.1")
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Planned Features Dialog
    if (showPlannedFeaturesDialog) {
        AlertDialog(
            onDismissRequest = { showPlannedFeaturesDialog = false },
            title = { Text("Planned Features") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Coming Soon:", fontWeight = FontWeight.Bold)
                    Text("• Native in app dark mode support")
                    Text("• Possible SQLite or a cloud based database")
                    Text("• Voice Transcription")
                    Text("• Better interface stuff")
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlannedFeaturesDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data?") },
            text = {
                Text("This will permanently delete all your journal entries and calendar markings. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearData()
                        showClearDataDialog = false
                    }
                ) {
                    Text("Clear", color = Color(0xFFEF5350))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import Success Dialog
    if (showImportSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showImportSuccessDialog = false },
            title = { Text("Import Successful") },
            text = {
                Text("Your data has been successfully imported.")
            },
            confirmButton = {
                TextButton(onClick = { showImportSuccessDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Import Error Dialog
    if (showImportErrorDialog) {
        AlertDialog(
            onDismissRequest = { showImportErrorDialog = false },
            title = { Text("Import Failed") },
            text = {
                Text("Failed to import data. Please make sure you selected a valid backup file.")
            },
            confirmButton = {
                TextButton(onClick = { showImportErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
