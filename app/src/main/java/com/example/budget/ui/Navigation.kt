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
    object ExpenseHistory : Screen("expense_history") {
        fun createRoute(month: Int, year: Int) = "expense_history/$month/$year"
        const val routeWithArgs = "expense_history/{month}/{year}"
    }
} 