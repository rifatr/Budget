package com.example.budget.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetRepository
import com.example.budget.data.ValidationConstants
import com.example.budget.data.db.Category
import com.example.budget.data.db.Expense
import com.example.budget.data.preferences.CategoryPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Date

data class ExpenseUiState(
    val allCategories: List<Category> = emptyList(),
    val date: Date = Date(),
    val category: Category? = null,
    val amount: String = "",
    val description: String = "",
    val isEntryValid: Boolean = false,
    val expenseHistory: List<Expense> = emptyList(),
    val showHistory: Boolean = false,
    val categoryMap: Map<Int, String> = emptyMap(),
    val showConfirmationMessage: Boolean = false,
    val confirmationMessage: String = ""
)

class ExpenseViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryPreferences: CategoryPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                budgetRepository.getAllCategories(), // Use alphabetical order instead of usage-based
                categoryPreferences.lastSelectedCategoryId
            ) { categories, lastSelectedCategoryId ->
                val currentCategory = _uiState.value.category
                val updatedCategory = when {
                    // Keep current selection if it still exists
                    currentCategory != null && categories.contains(currentCategory) -> currentCategory
                    // Try to select the last selected category if it exists
                    lastSelectedCategoryId != null -> categories.find { it.id == lastSelectedCategoryId }
                    // Fallback to first category alphabetically
                    else -> categories.firstOrNull()
                }
                
                _uiState.value = _uiState.value.copy(
                    allCategories = categories,
                    category = updatedCategory
                )
                validateInput()
            }.collect {}
        }
    }

    fun onDateChange(newDate: Date) {
        _uiState.value = _uiState.value.copy(date = newDate)
        validateInput()
    }

    fun onCategoryChange(newCategory: Category) {
        _uiState.value = _uiState.value.copy(category = newCategory)
        // Save the selected category as the last selected
        categoryPreferences.setLastSelectedCategoryId(newCategory.id)
        validateInput()
    }

    fun onAmountChange(newAmount: String) {
        // Enforce amount validation using constants
        if (newAmount.matches(ValidationConstants.AMOUNT_VALIDATION_REGEX)) {
            _uiState.value = _uiState.value.copy(amount = newAmount)
            validateInput()
        }
    }

    fun onDescriptionChange(newDescription: String) {
        // Limit description to specified length
        if (newDescription.length <= ValidationConstants.EXPENSE_DESCRIPTION_MAX_LENGTH) {
            _uiState.value = _uiState.value.copy(description = newDescription)
        }
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
            budgetRepository.incrementCategoryUsage(category.id)
            
            // Show success message
            _uiState.value = _uiState.value.copy(
                amount = "",
                description = "",
                showConfirmationMessage = true,
                confirmationMessage = "Expense of ${category.name} added successfully!"
            )
            validateInput()
            
            // Hide success message after 3 seconds
            kotlinx.coroutines.delay(3000)
            _uiState.value = _uiState.value.copy(showConfirmationMessage = false)
        }
    }

    private fun validateInput() {
        with(_uiState.value) {
            _uiState.value = copy(isEntryValid = category != null && amount.isNotBlank() && amount.toDoubleOrNull() != null)
        }
    }

    fun showExpenseHistory() {
        viewModelScope.launch {
            val allExpenses = budgetRepository.getAllExpenses().first()
            val categories = budgetRepository.getAllCategories().first()
            val categoryMap = categories.associate { it.id to it.name }
            
            _uiState.value = _uiState.value.copy(
                expenseHistory = allExpenses.sortedByDescending { it.date },
                categoryMap = categoryMap,
                showHistory = true
            )
        }
    }

    fun hideExpenseHistory() {
        _uiState.value = _uiState.value.copy(showHistory = false)
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            budgetRepository.deleteExpense(expense)
            showExpenseHistory() // Refresh the list
        }
    }
    
    fun dismissConfirmationMessage() {
        _uiState.value = _uiState.value.copy(showConfirmationMessage = false)
    }
} 