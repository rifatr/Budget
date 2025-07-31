package com.example.budget.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.budget.ui.budget.BudgetScreen
import com.example.budget.ui.expense.ExpenseScreen
import com.example.budget.ui.home.HomeScreen
import com.example.budget.ui.info.InfoScreen
import com.example.budget.ui.settings.SettingsScreen
import com.example.budget.ui.summary.SummaryScreen

@Composable
fun BudgetAppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(
            route = Screen.Budget.route,
            arguments = listOf(
                navArgument("month") { type = NavType.IntType },
                navArgument("year") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val month = backStackEntry.arguments?.getInt("month") ?: 0
            val year = backStackEntry.arguments?.getInt("year") ?: 0
            BudgetScreen(navController, month, year)
        }
        composable(Screen.Expense.route) {
            ExpenseScreen(navController)
        }
        composable(
            route = Screen.Summary.route,
            arguments = listOf(
                navArgument("month") { type = NavType.IntType },
                navArgument("year") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val month = backStackEntry.arguments?.getInt("month") ?: 0
            val year = backStackEntry.arguments?.getInt("year") ?: 0
            SummaryScreen(navController, month, year)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        composable(Screen.Info.route) {
            InfoScreen(navController)
        }
    }
} 