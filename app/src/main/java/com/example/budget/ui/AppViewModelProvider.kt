package com.example.budget.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.budget.BudgetApp
import com.example.budget.ui.budget.BudgetViewModel
import com.example.budget.ui.categorymanager.CategoryManagerViewModel
import com.example.budget.ui.expense.ExpenseViewModel
import com.example.budget.ui.expensehistory.ExpenseHistoryViewModel
import com.example.budget.ui.settings.SettingsViewModel
import com.example.budget.ui.summary.SummaryViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for BudgetViewModel
        initializer {
            BudgetViewModel(budgetApplication().container.budgetRepository)
        }

        // Initializer for ExpenseViewModel
        initializer {
            ExpenseViewModel(
                budgetApplication().container.budgetRepository,
                budgetApplication().container.categoryPreferences
            )
        }

        // Initializer for SummaryViewModel
        initializer {
            SummaryViewModel(
                budgetApplication().container.budgetRepository,
                budgetApplication().container.summaryLayoutPreferences
            )
        }

        // Initializer for SettingsViewModel
        initializer {
            SettingsViewModel(budgetApplication().container.budgetRepository)
        }

        // Initializer for CategoryManagerViewModel
        initializer {
            CategoryManagerViewModel(budgetApplication().container.budgetRepository)
        }
        initializer {
            com.example.budget.ui.categoryexpensedetail.CategoryExpenseDetailViewModel(budgetApplication().container.budgetRepository)
        }
        
        // Initializer for ExpenseHistoryViewModel
        initializer {
            ExpenseHistoryViewModel(budgetApplication().container.budgetRepository)
        }
    }
}

fun CreationExtras.budgetApplication(): BudgetApp =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BudgetApp) 