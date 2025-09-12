package com.example.budget.ui.categoryexpensedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetRepository
import com.example.budget.data.db.Expense
import com.example.budget.data.DateConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

data class CategoryExpenseDetailUiState(
    val categoryId: Int = 0,
    val month: Int = 0,
    val year: Int = 0,
    val budgeted: Double = 0.0,
    val totalSpent: Double = 0.0,
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val showConfirmationMessage: Boolean = false,
    val confirmationMessage: String = "",
    val isConfirmationError: Boolean = false
)

class CategoryExpenseDetailViewModel(
    private val budgetRepository: BudgetRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategoryExpenseDetailUiState())
    val uiState: StateFlow<CategoryExpenseDetailUiState> = _uiState.asStateFlow()
    
    fun initialize(categoryId: Int, month: Int, year: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                categoryId = categoryId,
                month = month,
                year = year
            )
            
            val (startDate, endDate) = DateConstants.getMonthStartAndEndTimestamps(year, month)
            
            combine(
                budgetRepository.getBudgetForMonth(month, year),
                budgetRepository.getExpensesForMonth(startDate, endDate)
            ) { budget, expenses ->
                val categoryExpenses = expenses.filter { it.categoryId == categoryId }
                val budgeted = budget?.categoryBudgets?.get(categoryId) ?: 0.0
                val totalSpent = categoryExpenses.sumOf { it.amount }
                
                CategoryExpenseDetailUiState(
                    categoryId = categoryId,
                    month = month,
                    year = year,
                    budgeted = budgeted,
                    totalSpent = totalSpent,
                    expenses = categoryExpenses.sortedByDescending { it.date },
                    isLoading = false
                )
            }.collect {
                _uiState.value = it
            }
        }
    }
    
    fun deleteExpense(expenseId: Int) {
        viewModelScope.launch {
            try {
                budgetRepository.deleteExpenseById(expenseId)
                // Refresh data after deletion
                refreshData()
                // Show confirmation message after data refresh is complete
                showConfirmationMessage("Expense deleted successfully", isError = false)
            } catch (e: Exception) {
                showConfirmationMessage("Failed to delete expense", isError = true)
            }
        }
    }
    
    private suspend fun refreshData() {
        val currentState = _uiState.value
        val (startDate, endDate) = DateConstants.getMonthStartAndEndTimestamps(currentState.year, currentState.month)
        
        // Get fresh data
        val budget = budgetRepository.getBudgetForMonth(currentState.month, currentState.year).first()
        val expenses = budgetRepository.getExpensesForMonth(startDate, endDate).first()
        
        val categoryExpenses = expenses.filter { it.categoryId == currentState.categoryId }
        val budgeted = budget?.categoryBudgets?.get(currentState.categoryId) ?: 0.0
        val totalSpent = categoryExpenses.sumOf { it.amount }
        
        // Update only the data fields, preserve confirmation message state
        _uiState.value = _uiState.value.copy(
            budgeted = budgeted,
            totalSpent = totalSpent,
            expenses = categoryExpenses,
            isLoading = false
        )
    }
    
    private fun showConfirmationMessage(message: String, isError: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showConfirmationMessage = true,
                confirmationMessage = message,
                isConfirmationError = isError
            )
            // Auto-dismiss after delay (same as BudgetViewModel)
            val delayTime = if (isError) 4000L else 3000L // 4s for errors, 3s for success
            kotlinx.coroutines.delay(delayTime)
            _uiState.value = _uiState.value.copy(showConfirmationMessage = false)
        }
    }
    
    fun dismissConfirmationMessage() {
        _uiState.value = _uiState.value.copy(
            showConfirmationMessage = false,
            confirmationMessage = "",
            isConfirmationError = false
        )
    }
    
}

