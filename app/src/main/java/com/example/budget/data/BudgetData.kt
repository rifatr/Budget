package com.example.budget.data

import com.example.budget.data.db.Budget
import com.example.budget.data.db.Category
import com.example.budget.data.db.Expense

data class BudgetData(
    val categories: List<Category>,
    val expenses: List<Expense>,
    val budgets: List<Budget>
) 