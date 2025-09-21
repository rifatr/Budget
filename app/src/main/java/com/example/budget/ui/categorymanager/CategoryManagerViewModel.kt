package com.example.budget.ui.categorymanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetRepository
import com.example.budget.data.db.Category
import com.example.budget.data.ValidationConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale

data class CategoryWithStats(
    val category: Category,
    val expenseCount: Int,
    val totalAmount: Double
)

data class CategoryManagerUiState(
    val categories: List<CategoryWithStats> = emptyList(),
    val searchQuery: String = "",
    val sortOption: SortOption = SortOption.BY_NAME,
    val sortAscending: Boolean = true,
    val showConfirmationMessage: Boolean = false,
    val confirmationMessage: String = "",
    val isConfirmationError: Boolean = false,
    val isLoading: Boolean = false,
    // Validation state for dialogs
    val showDuplicateError: Boolean = false,
    val duplicateErrorMessage: String = ""
) {
    val filteredAndSortedCategories: List<CategoryWithStats>
        get() {
            val filtered = if (searchQuery.isBlank()) {
                categories
            } else {
                categories.filter { 
                    it.category.name.contains(searchQuery, ignoreCase = true) 
                }
            }
            
            val sorted = when (sortOption) {
                SortOption.BY_NAME -> if (sortAscending) {
                    filtered.sortedBy { it.category.name }
                } else {
                    filtered.sortedByDescending { it.category.name }
                }
                SortOption.BY_USAGE -> if (sortAscending) {
                    filtered.sortedBy { it.expenseCount }
                } else {
                    filtered.sortedByDescending { it.expenseCount }
                }
                SortOption.BY_AMOUNT -> if (sortAscending) {
                    filtered.sortedBy { it.totalAmount }
                } else {
                    filtered.sortedByDescending { it.totalAmount }
                }
            }
            
            return sorted
        }
}

enum class SortOption(val displayName: String) {
    BY_NAME("Name"),
    BY_USAGE("Usage Count"),
    BY_AMOUNT("Total Spent")
}

class CategoryManagerViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagerUiState())
    val uiState: StateFlow<CategoryManagerUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            budgetRepository.getAllCategories().collect { categories ->
                val categoriesWithStats = categories.map { category ->
                    val expenseCount = budgetRepository.getExpenseCountByCategory(category.id)
                    val totalAmount = budgetRepository.getTotalAmountByCategory(category.id)
                    CategoryWithStats(category, expenseCount, totalAmount)
                }
                
                _uiState.value = _uiState.value.copy(
                    categories = categoriesWithStats,
                    isLoading = false
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateSortOption(sortOption: SortOption) {
        _uiState.value = _uiState.value.copy(sortOption = sortOption)
    }

    fun toggleSort(sortOption: SortOption) {
        val currentState = _uiState.value
        if (currentState.sortOption == sortOption) {
            // Same option clicked - toggle ascending/descending
            _uiState.value = currentState.copy(sortAscending = !currentState.sortAscending)
        } else {
            // Different option clicked - switch to new option with ascending
            _uiState.value = currentState.copy(sortOption = sortOption, sortAscending = true)
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            try {
                val trimmedName = name.trim()
                
                // Validation
                if (trimmedName.isBlank()) {
                    showErrorMessage("Category name cannot be empty!")
                    return@launch
                }
                
                if (trimmedName.length > ValidationConstants.CATEGORY_NAME_MAX_LENGTH) {
                    showErrorMessage("Category name must be ${ValidationConstants.CATEGORY_NAME_MAX_LENGTH} characters or less!")
                    return@launch
                }
                
                // Check for duplicates
                val existingCategories = _uiState.value.categories.map { it.category.name.lowercase() }
                if (existingCategories.contains(trimmedName.lowercase())) {
                    showErrorMessage("Category '$trimmedName' already exists!")
                    return@launch
                }
                
                val newCategory = Category(name = trimmedName)
                budgetRepository.insertCategory(newCategory)
                showSuccessMessage("Category '$trimmedName' added successfully!")
                
            } catch (e: Exception) {
                showErrorMessage("Failed to add category: ${e.message}")
            }
        }
    }

    fun renameCategory(categoryId: Int, newName: String) {
        viewModelScope.launch {
            try {
                val trimmedName = newName.trim()
                val currentCategory = _uiState.value.categories.find { it.category.id == categoryId }?.category
                
                if (currentCategory == null) {
                    showErrorMessage("Category not found!")
                    return@launch
                }
                
                // Validation
                if (trimmedName.isBlank()) {
                    showErrorMessage("Category name cannot be empty!")
                    return@launch
                }
                
                if (trimmedName.length > ValidationConstants.CATEGORY_NAME_MAX_LENGTH) {
                    showErrorMessage("Category name must be ${ValidationConstants.CATEGORY_NAME_MAX_LENGTH} characters or less!")
                    return@launch
                }
                
                // Check for duplicates (excluding current category)
                val existingCategories = _uiState.value.categories
                    .filter { it.category.id != categoryId }
                    .map { it.category.name.lowercase() }
                if (existingCategories.contains(trimmedName.lowercase())) {
                    showErrorMessage("Category '$trimmedName' already exists!")
                    return@launch
                }
                
                budgetRepository.updateCategoryName(categoryId, trimmedName)
                showSuccessMessage("Category renamed to '$trimmedName' successfully!")
                
            } catch (e: Exception) {
                showErrorMessage("Failed to rename category: ${e.message}")
            }
        }
    }

    fun deleteCategory(categoryWithStats: CategoryWithStats) {
        viewModelScope.launch {
            try {
                val category = categoryWithStats.category
                val expenseCount = categoryWithStats.expenseCount
                
                if (expenseCount > 0) {
                    // This should only be called after double confirmation
                    budgetRepository.deleteExpensesByCategory(category.id)
                }
                
                budgetRepository.deleteCategory(category)
                
                val message = if (expenseCount > 0) {
                    "Category '${category.name}' and $expenseCount expenses deleted successfully!"
                } else {
                    "Category '${category.name}' deleted successfully!"
                }
                showSuccessMessage(message)
                
            } catch (e: Exception) {
                showErrorMessage("Failed to delete category: ${e.message}")
            }
        }
    }

    fun dismissConfirmationMessage() {
        _uiState.value = _uiState.value.copy(
            showConfirmationMessage = false,
            isConfirmationError = false
        )
    }
    
    fun validateCategoryName(name: String, excludeCategoryId: Int? = null): Boolean {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                showDuplicateError = false,
                duplicateErrorMessage = ""
            )
            return false
        }
        
        val isDuplicate = _uiState.value.categories.any { categoryWithStats ->
            categoryWithStats.category.name.equals(trimmedName, ignoreCase = true) &&
            categoryWithStats.category.id != excludeCategoryId
        }
        
        if (isDuplicate) {
            _uiState.value = _uiState.value.copy(
                showDuplicateError = true,
                duplicateErrorMessage = "Category already exists"
            )
            return false
        } else {
            _uiState.value = _uiState.value.copy(
                showDuplicateError = false,
                duplicateErrorMessage = ""
            )
            return true
        }
    }
    
    fun clearValidationErrors() {
        _uiState.value = _uiState.value.copy(
            showDuplicateError = false,
            duplicateErrorMessage = ""
        )
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
