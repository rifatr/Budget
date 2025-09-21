package com.example.budget.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetRepository
import com.example.budget.data.ValidationConstants
import com.example.budget.data.db.Budget
import com.example.budget.data.db.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

data class BudgetUiState(
    val selectedMonth: Int,
    val selectedYear: Int,
    val allCategories: List<Category> = emptyList(),
    val budget: Budget? = null,
    val totalBudgetInput: String = "",
    val categoryBudgets: Map<Int, Double> = emptyMap(),
    val showConfirmationMessage: Boolean = false,
    val confirmationMessage: String = "",
    val isConfirmationError: Boolean = false
) {
    // Calculate total categorized budget
    val totalCategorizedBudget: Double
        get() = categoryBudgets.values.sum()
    
    // Calculate remaining uncategorized budget
    val uncategorizedBudget: Double
        get() {
            val totalBudget = totalBudgetInput.toDoubleOrNull() ?: 0.0
            return maxOf(0.0, totalBudget - totalCategorizedBudget)
        }
    
    // Check if total budget is set
    val hasTotalBudget: Boolean
        get() = totalBudgetInput.isNotBlank() && (totalBudgetInput.toDoubleOrNull() ?: 0.0) > 0.0
}

class BudgetViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState(0, 0))
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    fun initialize(month: Int, year: Int) {
        _uiState.value = _uiState.value.copy(selectedMonth = month, selectedYear = year)
        loadBudgetData(month, year)
    }

    fun refreshData() {
        loadBudgetData(_uiState.value.selectedMonth, _uiState.value.selectedYear)
    }

    private fun loadBudgetData(month: Int, year: Int) {
        viewModelScope.launch {
            val categories = budgetRepository.getAllCategories().first()
            val budget = budgetRepository.getBudgetForMonth(month, year).first()
            
            // Clean up category budgets - remove budgets for deleted categories
            val validCategoryIds = categories.map { it.id }.toSet()
            val cleanedCategoryBudgets = (budget?.categoryBudgets ?: emptyMap())
                .filterKeys { categoryId -> validCategoryIds.contains(categoryId) }
            
            _uiState.value = _uiState.value.copy(
                allCategories = categories,
                budget = budget,
                totalBudgetInput = budget?.overallBudget?.let { formatNumberForInput(it) } ?: "",
                categoryBudgets = cleanedCategoryBudgets
            )
        }
    }

    fun updateTotalBudgetInput(newBudget: String) {
        if (newBudget.matches(ValidationConstants.AMOUNT_VALIDATION_REGEX)) {
            _uiState.value = _uiState.value.copy(totalBudgetInput = newBudget)
        }
    }

    fun updateCategoryBudget(categoryId: Int, newBudget: String) {
        if (newBudget.matches(ValidationConstants.AMOUNT_VALIDATION_REGEX)) {
            val newBudgetAmount = newBudget.toDoubleOrNull() ?: 0.0
            val newCategoryBudgets = _uiState.value.categoryBudgets.toMutableMap()
            newCategoryBudgets[categoryId] = newBudgetAmount
            _uiState.value = _uiState.value.copy(categoryBudgets = newCategoryBudgets)
        }
    }

    fun saveBudget() {
        viewModelScope.launch {
            try {
                val totalBudget = _uiState.value.totalBudgetInput.toDoubleOrNull() ?: 0.0
                val categoryBudgetsSum = _uiState.value.categoryBudgets.values.sum()
                
                // Validate total budget
                if (totalBudget <= 0.0) {
                    showErrorMessage("Please set a valid total budget amount!")
                    return@launch
                }
                
                // Check if category budgets exceed total budget
                if (categoryBudgetsSum > totalBudget) {
                    val remaining = totalBudget - categoryBudgetsSum
                    showErrorMessage("Category budgets exceed total budget by ${String.format(Locale.US, "%.2f", -remaining)}!")
                    return@launch
                }
                
                val newBudget = Budget(
                    id = _uiState.value.budget?.id ?: 0,
                    month = _uiState.value.selectedMonth,
                    year = _uiState.value.selectedYear,
                    overallBudget = totalBudget,
                    categoryBudgets = _uiState.value.categoryBudgets
                )
                
                budgetRepository.insertOrUpdateBudget(newBudget)
                
                // Refresh budget data to get the saved budget with correct ID
                val savedBudget = budgetRepository.getBudgetForMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear).first()
                _uiState.value = _uiState.value.copy(budget = savedBudget)
                
                // Show success message
                showSuccessMessage("Budget saved successfully!")
                
            } catch (e: Exception) {
                showErrorMessage("Failed to save budget: ${e.message}")
            }
        }
    }

    fun addCategory(categoryName: String, budgetAmount: Double = 0.0) {
        viewModelScope.launch {
            try {
                val trimmedName = categoryName.trim()
                
                // Validate category name
                if (trimmedName.isBlank()) {
                    showErrorMessage("Category name cannot be empty!")
                    return@launch
                }
                
                if (trimmedName.length > ValidationConstants.CATEGORY_NAME_MAX_LENGTH) {
                    showErrorMessage("Category name must be ${ValidationConstants.CATEGORY_NAME_MAX_LENGTH} characters or less!")
                    return@launch
                }
                
                // Check if category with this name already exists
                val existingCategories = budgetRepository.getAllCategories().first()
                val categoryExists = existingCategories.any { 
                    it.name.equals(trimmedName, ignoreCase = true) 
                }
                
                if (categoryExists) {
                    showErrorMessage("Category '$trimmedName' already exists!")
                    return@launch
                }
                
                // Validate budget amount if provided
                if (budgetAmount > 0.0) {
                    val totalBudget = _uiState.value.totalBudgetInput.toDoubleOrNull() ?: 0.0
                    val currentCategorizedBudget = _uiState.value.categoryBudgets.values.sum()
                    val remainingBudget = totalBudget - currentCategorizedBudget
                    
                    if (totalBudget <= 0.0) {
                        showErrorMessage("Please set a total budget first before adding category budgets!")
                        return@launch
                    }
                    
                    if (budgetAmount > remainingBudget) {
                        showErrorMessage("Budget amount (${String.format(Locale.US, "%.2f", budgetAmount)}) exceeds remaining budget (${String.format(Locale.US, "%.2f", remainingBudget)})!")
                        return@launch
                    }
                }
                
                val newCategory = Category(name = trimmedName)
                budgetRepository.insertCategory(newCategory)
                
                // Update the UI state with new categories
                val updatedCategories = budgetRepository.getAllCategories().first()
                val addedCategory = updatedCategories.find { it.name.equals(trimmedName, ignoreCase = true) }
                
                val updatedCategoryBudgets = _uiState.value.categoryBudgets.toMutableMap()
                if (addedCategory != null && budgetAmount > 0.0) {
                    updatedCategoryBudgets[addedCategory.id] = budgetAmount
                }
                
                _uiState.value = _uiState.value.copy(
                    allCategories = updatedCategories,
                    categoryBudgets = updatedCategoryBudgets
                )
                
                // If budget amount is provided, save the budget to database
                if (budgetAmount > 0.0) {
                    val totalBudget = _uiState.value.totalBudgetInput.toDoubleOrNull() ?: 0.0
                    val newBudget = Budget(
                        id = _uiState.value.budget?.id ?: 0,
                        month = _uiState.value.selectedMonth,
                        year = _uiState.value.selectedYear,
                        overallBudget = totalBudget,
                        categoryBudgets = updatedCategoryBudgets
                    )
                    budgetRepository.insertOrUpdateBudget(newBudget)
                    
                    // Refresh budget data to get the saved budget with correct ID
                    val savedBudget = budgetRepository.getBudgetForMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear).first()
                    _uiState.value = _uiState.value.copy(budget = savedBudget)
                }
                
                val message = if (budgetAmount > 0.0) {
                    "Category '$trimmedName' added with budget ${String.format(Locale.US, "%.2f", budgetAmount)}!"
                } else {
                    "Category '$trimmedName' added successfully!"
                }
                showSuccessMessage(message)
                
            } catch (e: Exception) {
                showErrorMessage("Failed to add category: ${e.message}")
            }
        }
    }
    
    fun dismissConfirmationMessage() {
        _uiState.value = _uiState.value.copy(
            showConfirmationMessage = false,
            isConfirmationError = false
        )
    }
    
    // Helper function to format numbers without scientific notation for input fields
    fun formatNumberForInput(amount: Double): String {
        return String.format(Locale.US, "%.2f", amount).removeSuffix(".00")
    }
    
    fun showCancelMessage(message: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showConfirmationMessage = true,
                isConfirmationError = false,
                confirmationMessage = "Cancelled: $message"
            )
            // Hide message after 2 seconds (shorter for cancel messages)
            kotlinx.coroutines.delay(2000)
            _uiState.value = _uiState.value.copy(showConfirmationMessage = false)
        }
    }
    

    
    fun saveCategoryBudget(categoryId: Int) {
        viewModelScope.launch {
            try {
                val totalBudget = _uiState.value.totalBudgetInput.toDoubleOrNull() ?: 0.0
                val currentCategoryBudget = _uiState.value.categoryBudgets[categoryId] ?: 0.0
                val categoryName = _uiState.value.allCategories.find { it.id == categoryId }?.name ?: "Category"
                
                // Validate total budget is set
                if (totalBudget <= 0.0) {
                    showErrorMessage("Please set a total budget first!")
                    // Reset to saved value (which is 0 if no budget set)
                    val savedBudget = budgetRepository.getBudgetForMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear).first()
                    val savedCategoryBudget = savedBudget?.categoryBudgets?.get(categoryId) ?: 0.0
                    val updatedBudgets = _uiState.value.categoryBudgets.toMutableMap()
                    updatedBudgets[categoryId] = savedCategoryBudget
                    _uiState.value = _uiState.value.copy(categoryBudgets = updatedBudgets)
                    return@launch
                }
                
                // Calculate what total would be with this category budget
                val otherCategoriesTotal = _uiState.value.categoryBudgets
                    .filterKeys { it != categoryId }
                    .values.sum()
                val newTotal = otherCategoriesTotal + currentCategoryBudget
                
                // Check if this category budget would exceed total budget
                if (newTotal > totalBudget) {
                    val excess = newTotal - totalBudget
                    showErrorMessage("$categoryName budget exceeds limit by ${String.format(Locale.US, "%.2f", excess)}!")
                    
                    // Reset to saved value from database
                    val savedBudget = budgetRepository.getBudgetForMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear).first()
                    val savedCategoryBudget = savedBudget?.categoryBudgets?.get(categoryId) ?: 0.0
                    val updatedBudgets = _uiState.value.categoryBudgets.toMutableMap()
                    updatedBudgets[categoryId] = savedCategoryBudget
                    _uiState.value = _uiState.value.copy(categoryBudgets = updatedBudgets)
                    return@launch
                }
                
                // Save the valid budget
                val newBudget = Budget(
                    id = _uiState.value.budget?.id ?: 0,
                    month = _uiState.value.selectedMonth,
                    year = _uiState.value.selectedYear,
                    overallBudget = totalBudget,
                    categoryBudgets = _uiState.value.categoryBudgets
                )
                
                budgetRepository.insertOrUpdateBudget(newBudget)
                
                // Refresh budget data to get the saved budget with correct ID
                val savedBudget = budgetRepository.getBudgetForMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear).first()
                _uiState.value = _uiState.value.copy(budget = savedBudget)
                
                showSuccessMessage("$categoryName budget (${String.format(Locale.US, "%.2f", currentCategoryBudget)}) saved successfully!")
                
            } catch (e: Exception) {
                showErrorMessage("Failed to save category budget: ${e.message}")
                
                // Reset to saved value on any error
                try {
                    val savedBudget = budgetRepository.getBudgetForMonth(_uiState.value.selectedMonth, _uiState.value.selectedYear).first()
                    val savedCategoryBudget = savedBudget?.categoryBudgets?.get(categoryId) ?: 0.0
                    val updatedBudgets = _uiState.value.categoryBudgets.toMutableMap()
                    updatedBudgets[categoryId] = savedCategoryBudget
                    _uiState.value = _uiState.value.copy(categoryBudgets = updatedBudgets)
                } catch (resetError: Exception) {
                    // If reset fails, just log it
                }
            }
        }
    }
    
    private suspend fun showSuccessMessage(message: String) {
        _uiState.value = _uiState.value.copy(
            showConfirmationMessage = true,
            confirmationMessage = message,
            isConfirmationError = false
        )
        // Hide message after 3 seconds
        kotlinx.coroutines.delay(3000)
        _uiState.value = _uiState.value.copy(showConfirmationMessage = false)
    }
    
    private suspend fun showErrorMessage(message: String) {
        _uiState.value = _uiState.value.copy(
            showConfirmationMessage = true,
            confirmationMessage = message,
            isConfirmationError = true
        )
        // Hide message after 4 seconds for errors (longer to read)
        kotlinx.coroutines.delay(4000)
        _uiState.value = _uiState.value.copy(showConfirmationMessage = false)
    }
} 