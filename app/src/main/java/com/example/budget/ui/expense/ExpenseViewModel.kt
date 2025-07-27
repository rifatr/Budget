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
    val description: String = "",
    val isEntryValid: Boolean = false
)

class ExpenseViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                allCategories = budgetRepository.getAllCategories().first()
            )
            validateInput()
        }
    }

    fun onDateChange(newDate: Date) {
        _uiState.value = _uiState.value.copy(date = newDate)
        validateInput()
    }

    fun onCategoryChange(newCategory: Category) {
        _uiState.value = _uiState.value.copy(category = newCategory)
        validateInput()
    }

    fun onAmountChange(newAmount: String) {
        // Enforce 6 digits before the decimal point and 2 digits after
        if (newAmount.matches(Regex("^\\d{0,6}(\\.\\d{0,2})?\$"))) {
            _uiState.value = _uiState.value.copy(amount = newAmount)
            validateInput()
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.value = _uiState.value.copy(description = newDescription)
    }

    fun saveExpense() {
        if (!uiState.value.isEntryValid) return

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

    private fun validateInput() {
        with(_uiState.value) {
            _uiState.value = copy(isEntryValid = category != null && amount.isNotBlank() && amount.toDoubleOrNull() != null)
        }
    }
} 