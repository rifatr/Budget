package com.example.budget.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.budget.ui.budget.BudgetScreen
import com.example.budget.ui.expense.ExpenseScreen
import com.example.budget.ui.info.InfoScreen
import com.example.budget.ui.more.MoreScreen
import com.example.budget.ui.settings.SettingsScreen
import com.example.budget.ui.summary.SummaryScreen
import java.util.*

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetAppNavigation(
    navController: NavHostController = rememberNavController(),
    startWithExpenseScreen: Boolean = false
) {
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Expense, Icons.Default.MonetizationOn, "Expense"),
        BottomNavItem(Screen.Budget, Icons.Default.AccountBalanceWallet, "Budget"),
        BottomNavItem(Screen.Summary, Icons.Default.Assessment, "Summary"),
        BottomNavItem(Screen.More, Icons.Default.MoreHoriz, "More")
    )

    // Navigate to expense screen if opened from widget
    LaunchedEffect(startWithExpenseScreen) {
        if (startWithExpenseScreen) {
            navController.navigate(Screen.Expense.route) {
                popUpTo(Screen.Expense.route) {
                    inclusive = true
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                item.icon, 
                                contentDescription = item.label,
                                tint = if (currentRoute == item.screen.route) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        },
                        label = { 
                            Text(
                                item.label,
                                color = if (currentRoute == item.screen.route) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        },
                        selected = currentRoute == item.screen.route,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                // Pop up to the start destination to avoid large back stack
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when re-selecting tab
                                launchSingleTop = true
                                // Restore state when re-selecting a previously selected tab
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Expense.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Expense.route) {
                ExpenseScreen()
            }
            composable(Screen.Budget.route) {
                BudgetScreen()
            }
            composable(Screen.Summary.route) {
                SummaryScreen()
            }
            composable(Screen.More.route) {
                MoreScreen(navController)
            }
            composable(Screen.Info.route) {
                InfoScreen(navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController)
            }
        }
    }
} 