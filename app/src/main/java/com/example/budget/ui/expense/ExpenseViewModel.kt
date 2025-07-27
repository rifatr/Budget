package com.example.budget.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetRepository
import com.example.budget.data.db.Category
import com.example.budget.data.db.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

data class ExpenseUiState(
    val allCategories: List<Category> = emptyList(),
    val date: Date = Date(),
    val category: Category? = null,
    val amount: String = "",
    val description: String = ""
)

class ExpenseViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                allCategories = budgetRepository.getAllCategories().first()
            )
        }
    }

    fun onDateChange(newDate: Date) {
        _uiState.value = _uiState.value.copy(date = newDate)
    }

    fun onCategoryChange(newCategory: Category) {
        _uiState.value = _uiState.value.copy(category = newCategory)
    }

    fun onAmountChange(newAmount: String) {
        _uiState.value = _uiState.value.copy(amount = newAmount)
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun saveExpense() {
        viewModelScope.launch {
            val amount = _uiState.value.amount.toDoubleOrNull() ?: return@launch
            val category = _uiState.value.category ?: return@launch
            val newExpense = Expense(
                date = _uiState.value.date,
                categoryId = category.id,
                amount = amount,
                description = _uiState.value.description
            )
            budgetRepository.insertExpense(newExpense)
        }
    }
} 