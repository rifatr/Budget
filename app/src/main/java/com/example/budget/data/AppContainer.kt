package com.example.budget.data

import android.content.Context
import com.example.budget.data.db.AppDatabase
import com.example.budget.data.preferences.CurrencyPreferences
import com.example.budget.data.preferences.SummaryLayoutPreferences
import com.example.budget.data.preferences.CategoryPreferences

interface AppContainer {
    val budgetRepository: BudgetRepository
    val currencyPreferences: CurrencyPreferences
    val summaryLayoutPreferences: SummaryLayoutPreferences
    val categoryPreferences: CategoryPreferences
}

class AppDataContainer(private val context: Context) : AppContainer {
    override val budgetRepository: BudgetRepository by lazy {
        BudgetRepository(
            AppDatabase.getDatabase(context).categoryDao(),
            AppDatabase.getDatabase(context).expenseDao(),
            AppDatabase.getDatabase(context).budgetDao()
        )
    }
    
    override val currencyPreferences: CurrencyPreferences by lazy {
        CurrencyPreferences(context)
    }
    
    override val summaryLayoutPreferences: SummaryLayoutPreferences by lazy {
        SummaryLayoutPreferences(context)
    }
    
    override val categoryPreferences: CategoryPreferences by lazy {
        CategoryPreferences(context)
    }
} 