package com.example.budget.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetRepository
import com.example.budget.data.db.Budget
import com.example.budget.data.db.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class BudgetUiState(
    val selectedMonth: Int,
    val selectedYear: Int,
    val allCategories: List<Category> = emptyList(),
    val budget: Budget? = null,
    val totalBudgetInput: String = "",
    val categoryBudgets: Map<Int, Double> = emptyMap()
)

class BudgetViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState(0, 0))
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    fun initialize(month: Int, year: Int) {
        _uiState.value = _uiState.value.copy(selectedMonth = month, selectedYear = year)
        viewModelScope.launch {
            val categories = budgetRepository.getAllCategories().first()
            val budget = budgetRepository.getBudgetForMonth(month, year).first()
            _uiState.value = _uiState.value.copy(
                allCategories = categories,
                budget = budget,
                totalBudgetInput = budget?.overallBudget?.toString() ?: "",
                categoryBudgets = budget?.categoryBudgets ?: emptyMap()
            )
        }
    }

    fun updateTotalBudgetInput(newBudget: String) {
        if (newBudget.matches(Regex("^\\d{0,6}(\\.\\d{0,2})?\$"))) {
            _uiState.value = _uiState.value.copy(totalBudgetInput = newBudget)
        }
    }

    fun updateCategoryBudget(categoryId: Int, newBudget: String) {
        if (newBudget.matches(Regex("^\\d{0,6}(\\.\\d{0,2})?\$"))) {
            val newCategoryBudgets = _uiState.value.categoryBudgets.toMutableMap()
            newCategoryBudgets[categoryId] = newBudget.toDoubleOrNull() ?: 0.0
            _uiState.value = _uiState.value.copy(categoryBudgets = newCategoryBudgets)
        }
    }

    fun saveBudget() {
        viewModelScope.launch {
            val totalBudget = _uiState.value.totalBudgetInput.toDoubleOrNull() ?: 0.0
            val newBudget = Budget(
                id = _uiState.value.budget?.id ?: 0,
                month = _uiState.value.selectedMonth,
                year = _uiState.value.selectedYear,
                overallBudget = totalBudget,
                categoryBudgets = _uiState.value.categoryBudgets
            )
            budgetRepository.insertOrUpdateBudget(newBudget)
        }
    }

    fun addCategory(categoryName: String, budgetAmount: Double = 0.0) {
        viewModelScope.launch {
            // Check if category with this name already exists
            val existingCategories = budgetRepository.getAllCategories().first()
            val categoryExists = existingCategories.any { 
                it.name.equals(categoryName.trim(), ignoreCase = true) 
            }
            
            if (!categoryExists && categoryName.trim().isNotBlank()) {
                val newCategory = Category(name = categoryName.trim())
                budgetRepository.insertCategory(newCategory)
                
                // Update the UI state with new categories
                val updatedCategories = budgetRepository.getAllCategories().first()
                val addedCategory = updatedCategories.find { it.name.equals(categoryName.trim(), ignoreCase = true) }
                
                val updatedCategoryBudgets = _uiState.value.categoryBudgets.toMutableMap()
                if (addedCategory != null && budgetAmount > 0.0) {
                    updatedCategoryBudgets[addedCategory.id] = budgetAmount
                }
                
                _uiState.value = _uiState.value.copy(
                    allCategories = updatedCategories,
                    categoryBudgets = updatedCategoryBudgets
                )
            }
        }
    }
} 