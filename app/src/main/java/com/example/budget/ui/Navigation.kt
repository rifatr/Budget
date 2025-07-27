package com.example.budget.ui

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Budget : Screen("budget/{month}/{year}") {
        fun createRoute(month: Int, year: Int) = "budget/$month/$year"
    }
    object Expense : Screen("expense")
    object Summary : Screen("summary/{month}/{year}") {
        fun createRoute(month: Int, year: Int) = "summary/$month/$year"
    }
    object Settings : Screen("settings")
} 