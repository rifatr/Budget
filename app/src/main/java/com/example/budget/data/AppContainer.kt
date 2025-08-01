package com.example.budget.data

import android.content.Context
import com.example.budget.data.db.AppDatabase

interface AppContainer {
    val budgetRepository: BudgetRepository
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val budgetRepository: BudgetRepository by lazy {
        BudgetRepository(
            AppDatabase.getDatabase(context).categoryDao(),
            AppDatabase.getDatabase(context).expenseDao(),
            AppDatabase.getDatabase(context).budgetDao()
        )
    }
} 