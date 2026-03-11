package com.yourname.challengetracker.MainScreens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onDateClick: (String) -> Unit,
    daysWithEntries: Set<LocalDate> = emptySet(),
    coloredDays: Map<LocalDate, Color> = emptyMap(),
    onColoredDaysChange: (Map<LocalDate, Color>) -> Unit = {},
    colorIntensity: Float = 1.0f,
    onColorIntensityChange: (Float) -> Unit = {}
) {
    var selectedDates by remember { mutableStateOf(setOf<LocalDate>()) }
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusYears(2) }
    val endMonth = remember { currentMonth.plusYears(2) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    var showColorDialog by remember { mutableStateOf(false) }

    // THEME-AWARE GREY COLORS
    val isDarkTheme = isSystemInDarkTheme()
    val appGreyColor = if (isDarkTheme) {
        Color(0xFF424242)
    } else {
        Color(0xFFE0E0E0)
    }

    val disabledButtonColor = if (isDarkTheme) {
        Color(0xFF303030)
    } else {
        Color(0xFFE0E0E0)
    }

    val baseColorOptions = listOf(
        Color(0xFFEF9A9A), // Muted Red
        Color(0xFFA5D6A7), // Muted Green
        Color(0xFF90CAF9), // Muted Blue
        Color(0xFFFFF59D), // Muted Yellow
        Color(0xFFFFCC80), // Muted Orange
        Color(0xFFCE93D8), // Muted Purple
        Color(0xFF9FA8DA)  // Muted Indigo
    )

    // Apply intensity to color options for display
    val colorOptions = baseColorOptions.map { color ->
        adjustColorIntensity(color, colorIntensity, isDarkTheme)
    }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Calendar",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Calendar fills available space
            VerticalCalendar(
                state = state,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
                dayContent = { day ->
                    Day(
                        day = day,
                        isSelected = selectedDates.contains(day.date),
                        dayColor = coloredDays[day.date]?.let {
                            adjustColorIntensity(it, colorIntensity, isDarkTheme)
                        },
                        hasJournalEntry = daysWithEntries.contains(day.date),
                        selectionColor = appGreyColor,
                        onClick = {
                            if (selectedDates.contains(day.date)) {
                                selectedDates = selectedDates - day.date
                            } else {
                                selectedDates = selectedDates + day.date
                            }
                        }
                    )
                },
                monthHeader = { month ->
                    MonthHeader(month)
                }
            )

            // Buttons overlaid at bottom
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .offset(y = 36.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (selectedDates.size == 1) {
                            onDateClick(selectedDates.first().toString())
                        }
                    },
                    enabled = selectedDates.size == 1,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = disabledButtonColor
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Add Entry",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Entry")
                }

                Button(
                    onClick = {
                        if (selectedDates.isNotEmpty()) {
                            showColorDialog = true
                        }
                    },
                    enabled = selectedDates.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        disabledContainerColor = disabledButtonColor
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Color",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Color")
                }
            }
        }
    }

    if (showColorDialog && selectedDates.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = when (selectedDates.size) {
                        1 -> "Choose Color for ${selectedDates.first().format(DateTimeFormatter.ofPattern("MMM d"))}"
                        else -> "Choose Color for ${selectedDates.size} dates"
                    },
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Color intensity slider
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Color Intensity",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Slider(
                            value = colorIntensity,
                            onValueChange = { newIntensity ->
                                onColorIntensityChange(newIntensity)
                            },
                            valueRange = 0.3f..1.0f,
                            modifier = Modifier.fillMaxWidth(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Subtle",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "Vibrant",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Color selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        colorOptions.take(4).forEachIndexed { index, displayColor ->
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(displayColor)
                                    .clickable {
                                        val newColoredDays = coloredDays.toMutableMap()
                                        selectedDates.forEach { date ->
                                            newColoredDays[date] = baseColorOptions[index]
                                        }
                                        onColoredDaysChange(newColoredDays)
                                        showColorDialog = false
                                        selectedDates = emptySet()
                                    }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        colorOptions.drop(4).forEachIndexed { index, displayColor ->
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(displayColor)
                                    .clickable {
                                        val newColoredDays = coloredDays.toMutableMap()
                                        selectedDates.forEach { date ->
                                            newColoredDays[date] = baseColorOptions[index + 4]
                                        }
                                        onColoredDaysChange(newColoredDays)
                                        showColorDialog = false
                                        selectedDates = emptySet()
                                    }
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            val newColoredDays = coloredDays.toMutableMap()
                            selectedDates.forEach { date ->
                                newColoredDays.remove(date)
                            }
                            onColoredDaysChange(newColoredDays)
                            showColorDialog = false
                            selectedDates = emptySet()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Remove Color", color = MaterialTheme.colorScheme.secondary)
                    }

                    TextButton(
                        onClick = {
                            selectedDates = emptySet()
                            showColorDialog = false
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Clear Selection", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
