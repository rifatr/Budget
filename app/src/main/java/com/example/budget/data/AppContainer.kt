package com.example.budget.data

import android.content.Context
import com.example.budget.data.db.AppDatabase
import com.example.budget.data.preferences.CurrencyPreferences
import com.example.budget.data.preferences.SummaryLayoutPreferences

interface AppContainer {
    val budgetRepository: BudgetRepository
    val currencyPreferences: CurrencyPreferences
    val summaryLayoutPreferences: SummaryLayoutPreferences
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
} 