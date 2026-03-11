package com.yourname.challengetracker.MainScreens.uinavtheme.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Data class representing a bottom navigation item.
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * List of bottom navigation items.
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = NavigationRoutes.Calendar.route,
        icon = Icons.Default.DateRange,
        label = "Calendar"
    ),
    BottomNavItem(
        route = NavigationRoutes.JournalList.route,
        icon = Icons.Default.List,
        label = "Journal"
    ),
    BottomNavItem(
        route = NavigationRoutes.Settings.route,
        icon = Icons.Default.Settings,
        label = "Settings"
    )
)

/**
 * Bottom navigation bar composable.
 */
@Composable
fun BottomNavBar(
    navController: NavController,
    currentDestination: NavDestination?
) {
    val isDarkTheme = isSystemInDarkTheme()

    NavigationBar(
        containerColor = if (isDarkTheme) {
            Color(0xFF1B1B1B)
        } else {
            Color.White
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (isDarkTheme) Color.White else Color.Black,
                    selectedTextColor = if (isDarkTheme) Color.White else Color.Black,
                    indicatorColor = if (isDarkTheme) {
                        Color(0xFF424242)
                    } else {
                        Color(0xFFE0E0E0)
                    },
                    unselectedIconColor = if (isDarkTheme) {
                        Color(0xFF9E9E9E)
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
