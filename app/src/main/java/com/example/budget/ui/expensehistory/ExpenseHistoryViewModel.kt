package com.example.budget.ui.expensehistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetRepository
import com.example.budget.data.DateConstants
import com.example.budget.data.db.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ExpenseHistoryUiState(
    val expenses: List<Expense> = emptyList(),
    val categoryMap: Map<Int, String> = emptyMap(),
    val selectedMonth: Int = DateConstants.getCurrentMonth(),
    val selectedYear: Int = DateConstants.getCurrentYear(),
    val totalExpenses: Double = 0.0,
    val showConfirmationMessage: Boolean = false,
    val confirmationMessage: String = "",
    val isConfirmationError: Boolean = false
)

class ExpenseHistoryViewModel(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseHistoryUiState())
    val uiState: StateFlow<ExpenseHistoryUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
        loadCategories()
    }

    fun updateMonthYear(month: Int, year: Int) {
        _uiState.value = _uiState.value.copy(
            selectedMonth = month,
            selectedYear = year
        )
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            val (monthStart, monthEnd) = DateConstants.getMonthStartAndEndTimestamps(
                _uiState.value.selectedYear,
                _uiState.value.selectedMonth
            )
            
            val expenses = budgetRepository.getExpensesForMonth(monthStart, monthEnd).first()
            val sortedExpenses = expenses.sortedByDescending { it.date }
            val totalExpenses = expenses.sumOf { it.amount }
            
            _uiState.value = _uiState.value.copy(
                expenses = sortedExpenses,
                totalExpenses = totalExpenses
            )
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categories = budgetRepository.getAllCategories().first()
            val categoryMap = categories.associate { it.id to it.name }
            
            _uiState.value = _uiState.value.copy(
                categoryMap = categoryMap
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            budgetRepository.deleteExpense(expense)
            
            // Show success message
            showConfirmationMessage("Expense deleted successfully!", false)
            
            // Refresh expenses
            loadExpenses()
        }
    }

    private fun showConfirmationMessage(message: String, isError: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showConfirmationMessage = true,
                confirmationMessage = message,
                isConfirmationError = isError
            )
            
            // Auto-dismiss after delay
            kotlinx.coroutines.delay(if (isError) 4000 else 3000)
            _uiState.value = _uiState.value.copy(showConfirmationMessage = false)
        }
    }

    fun dismissConfirmationMessage() {
        _uiState.value = _uiState.value.copy(showConfirmationMessage = false)
    }
}
