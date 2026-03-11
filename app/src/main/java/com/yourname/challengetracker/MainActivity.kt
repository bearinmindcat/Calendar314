package com.yourname.challengetracker

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourname.challengetracker.data.DataManager
import com.yourname.challengetracker.data.model.AppData
import com.yourname.challengetracker.data.model.ColoredDayData
import com.yourname.challengetracker.data.model.JournalEntry
import com.yourname.challengetracker.data.model.JournalEntryData
import com.yourname.challengetracker.MainScreens.calendar.CalendarScreen
import com.yourname.challengetracker.MainScreens.journal.JournalEntryScreen
import com.yourname.challengetracker.MainScreens.journal.JournalListScreen
import com.yourname.challengetracker.MainScreens.settings.SettingsScreen
import com.yourname.challengetracker.MainScreens.uinavtheme.theme.Calendar314Theme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Calendar314Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current
    val dataManager = remember { DataManager(context) }
    val scope = rememberCoroutineScope()

    // Check if dark mode is enabled
    val isDarkTheme = isSystemInDarkTheme()

    // Load saved data on startup
    var savedData by remember { mutableStateOf(dataManager.loadData()) }

    // Convert saved data to working data structures
    var journalEntries by remember {
        mutableStateOf(
            savedData.journalEntries.mapNotNull { entry ->
                try {
                    JournalEntry(
                        date = LocalDate.parse(entry.date),
                        title = entry.title,
                        content = entry.content
                    )
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to parse journal entry date: ${entry.date}", e)
                    null
                }
            }
        )
    }

    var coloredDays by remember {
        mutableStateOf(
            savedData.coloredDays.mapNotNull { coloredDay ->
                try {
                    val date = LocalDate.parse(coloredDay.date)
                    val colorInt = coloredDay.colorHex.toLong(16).toInt()
                    date to Color(colorInt)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to parse colored day: ${coloredDay.date}, ${coloredDay.colorHex}", e)
                    null
                }
            }.toMap()
        )
    }

    // Load saved color intensity
    var colorIntensity by remember { mutableStateOf(savedData.colorIntensity) }

    // Derive days with entries from journal entries
    val daysWithEntries = journalEntries.map { it.date }.toSet()

    // Save data whenever it changes
    fun saveAllData() {
        scope.launch {
            try {
                val appData = AppData(
                    journalEntries = journalEntries.map { entry ->
                        JournalEntryData(
                            date = entry.date.toString(),
                            title = entry.title,
                            content = entry.content
                        )
                    },
                    coloredDays = coloredDays.map { (date, color) ->
                        val argb = color.toArgb()
                        ColoredDayData(
                            date = date.toString(),
                            colorHex = String.format("%08X", argb)
                        )
                    },
                    colorIntensity = colorIntensity
                )
                dataManager.saveData(appData)
                Log.d("MainActivity", "Data saved successfully. Colors: ${coloredDays.size}, Entries: ${journalEntries.size}")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to save data", e)
            }
        }
    }

    // Export data to file
    fun exportData(uriString: String) {
        scope.launch {
            try {
                val uri = Uri.parse(uriString)
                val jsonData = dataManager.exportData()

                if (jsonData != null) {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(jsonData.toByteArray())
                        }
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Data exported successfully", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("Export", "Export failed", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Import data from JSON string
    fun importData(jsonString: String): Boolean {
        return try {
            val success = dataManager.importData(jsonString)
            if (success) {
                // Reload data after successful import
                savedData = dataManager.loadData()

                journalEntries = savedData.journalEntries.mapNotNull { entry ->
                    try {
                        JournalEntry(
                            date = LocalDate.parse(entry.date),
                            title = entry.title,
                            content = entry.content
                        )
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to parse imported journal entry", e)
                        null
                    }
                }

                coloredDays = savedData.coloredDays.mapNotNull { coloredDay ->
                    try {
                        val date = LocalDate.parse(coloredDay.date)
                        val colorInt = coloredDay.colorHex.toLong(16).toInt()
                        date to Color(colorInt)
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Failed to parse imported colored day", e)
                        null
                    }
                }.toMap()

                colorIntensity = savedData.colorIntensity

                Toast.makeText(context, "Data imported successfully", Toast.LENGTH_SHORT).show()
                true
            } else {
                Toast.makeText(context, "Import failed", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Log.e("Import", "Import failed", e)
            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(
            route = "calendar",
            icon = Icons.Default.DateRange,
            label = "Calendar"
        ),
        BottomNavItem(
            route = "journal_list",
            icon = Icons.Default.List,
            label = "Journal"
        ),
        BottomNavItem(
            route = "settings",
            icon = Icons.Default.Settings,
            label = "Settings"
        )
    )

    val showBottomBar = currentDestination?.route != "dream_note/{date}"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    // Navigation bar container color
                    containerColor = if (isDarkTheme) {
                        Color(0xFF1B1B1B) // Dark grey for dark mode
                    } else {
                        Color.White // White for light mode
                    },
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            // THEME-AWARE NAVIGATION INDICATOR COLORS
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isDarkTheme) Color.White else Color.Black,
                                selectedTextColor = if (isDarkTheme) Color.White else Color.Black,
                                indicatorColor = if (isDarkTheme) {
                                    Color(0xFF424242)  // Medium grey for dark mode
                                } else {
                                    Color(0xFFE0E0E0)  // Light grey for light mode (darker than before)
                                },
                                unselectedIconColor = if (isDarkTheme) {
                                    Color(0xFF9E9E9E)  // Lighter grey for unselected in dark mode
                                } else {
                                    Color.Gray
                                },
                                unselectedTextColor = if (isDarkTheme) {
                                    Color(0xFF9E9E9E)
                                } else {
                                    Color.Gray
                                }
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "calendar",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("calendar") {
                CalendarScreen(
                    onDateClick = { date ->
                        navController.navigate("dream_note/$date")
                    },
                    daysWithEntries = daysWithEntries,
                    coloredDays = coloredDays,
                    onColoredDaysChange = { newColoredDays ->
                        Log.d("MainActivity", "Colors changed: ${newColoredDays.size} days colored")
                        coloredDays = newColoredDays
                        saveAllData()
                    },
                    colorIntensity = colorIntensity,
                    onColorIntensityChange = { newIntensity ->
                        colorIntensity = newIntensity
                        saveAllData()
                    }
                )
            }
            composable("journal_list") {
                JournalListScreen(
                    onEntryClick = { date ->
                        navController.navigate("dream_note/$date")
                    },
                    journalEntries = journalEntries
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onClearData = {
                        journalEntries = emptyList()
                        coloredDays = emptyMap()
                        colorIntensity = 1.0f  // Reset intensity to default
                        dataManager.clearAllData()
                        Toast.makeText(context, "All data cleared", Toast.LENGTH_SHORT).show()
                    },
                    onExportData = { uriString ->
                        exportData(uriString)
                    },
                    onImportData = { jsonString ->
                        importData(jsonString)
                    }
                )
            }
            composable("dream_note/{date}") { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: ""

                val existingEntry = try {
                    val localDate = LocalDate.parse(date)
                    journalEntries.find { it.date == localDate }
                } catch (e: Exception) {
                    null
                }

                JournalEntryScreen(
                    date = date,
                    initialTitle = existingEntry?.title ?: "",
                    initialContent = existingEntry?.content ?: "",
                    onBack = { navController.popBackStack() },
                    onSave = { title, content, audioPath ->  // Fixed: 3 parameters
                        try {
                            val localDate = LocalDate.parse(date)

                            // Remove existing entry for this date if it exists
                            journalEntries = journalEntries.filter { it.date != localDate }

                            // Add new/updated entry
                            journalEntries = journalEntries + JournalEntry(
                                date = localDate,
                                title = title,
                                content = content
                            )

                            // Save to JSON
                            saveAllData()

                            Log.d("DEBUG", "Saved entry for date: $localDate")
                            Log.d("DEBUG", "Total entries: ${journalEntries.size}")
                            // Note: audioPath is received but not stored yet
                            if (audioPath != null) {
                                Log.d("DEBUG", "Audio was recorded but not stored in data model yet")
                            }
                        } catch (e: Exception) {
                            Log.e("DEBUG", "Failed to parse date: $date", e)
                        }
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)