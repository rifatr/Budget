package com.example.budget.ui

sealed class Screen(val route: String) {
    object Expense : Screen("expense")
    object Budget : Screen("budget")
    object Summary : Screen("summary")
    object More : Screen("more")
    object Info : Screen("info")
    object Settings : Screen("settings")
    object CategoryManager : Screen("category_manager")
    object CategoryExpenseDetail : Screen("category_expense_detail")
} 