package com.example.budget.ui.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetRepository
import com.example.budget.data.DateConstants
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
    val latestExpenses: List<Expense> = emptyList(),
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
                    category = updatedCategory,
                    categoryMap = categories.associate { it.id to it.name }
                )
                validateInput()
            }.collect {}
        }
        
        // Observe expenses changes automatically
        observeLatestExpenses()
    }

    fun onDateChange(newDate: Date) {
        _uiState.value = _uiState.value.copy(date = newDate)
        validateInput()
        // Refresh latest expenses for the new date
        viewModelScope.launch {
            val allExpenses = budgetRepository.getAllExpenses().first()
            updateLatestExpensesFromList(allExpenses)
        }
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
    
    private fun observeLatestExpenses() {
        viewModelScope.launch {
            // Observe all expenses and filter/update when they change
            budgetRepository.getAllExpenses().collect { allExpenses ->
                updateLatestExpensesFromList(allExpenses)
            }
        }
    }
    
    private fun updateLatestExpensesFromList(allExpenses: List<Expense>) {
        val currentDate = _uiState.value.date
        val calendar = java.util.Calendar.getInstance()
        calendar.time = currentDate
        val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        
        val (monthStart, monthEnd) = DateConstants.getMonthStartAndEndTimestamps(currentYear, currentMonth)
        
        // Filter expenses for current month and get latest expenses
        val monthExpenses = allExpenses.filter { expense ->
            expense.date.time >= monthStart.time && expense.date.time <= monthEnd.time
        }
        val latestExpenses = monthExpenses.sortedByDescending { it.date }.take(ValidationConstants.LATEST_EXPENSES_COUNT)
        
        _uiState.value = _uiState.value.copy(
            latestExpenses = latestExpenses
        )
    }
    
    
    fun dismissConfirmationMessage() {
        _uiState.value = _uiState.value.copy(showConfirmationMessage = false)
    }
} 