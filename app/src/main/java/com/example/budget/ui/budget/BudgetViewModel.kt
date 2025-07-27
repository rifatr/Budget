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
    val overallBudgetInput: String = "",
    val categoryBudgetsInput: Map<Int, String> = emptyMap()
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
                overallBudgetInput = budget?.overallBudget?.toString() ?: "",
                categoryBudgetsInput = budget?.categoryBudgets?.mapValues { it.value.toString() } ?: emptyMap()
            )
        }
    }

    fun onOverallBudgetChange(newBudget: String) {
        if (newBudget.matches(Regex("^\\d{0,6}(\\.\\d{0,2})?\$"))) {
            _uiState.value = _uiState.value.copy(overallBudgetInput = newBudget)
        }
    }

    fun onCategoryBudgetChange(categoryId: Int, newBudget: String) {
        if (newBudget.matches(Regex("^\\d{0,6}(\\.\\d{0,2})?\$"))) {
            val newCategoryBudgets = _uiState.value.categoryBudgetsInput.toMutableMap()
            newCategoryBudgets[categoryId] = newBudget
            _uiState.value = _uiState.value.copy(categoryBudgetsInput = newCategoryBudgets)
        }
    }

    fun saveBudget() {
        viewModelScope.launch {
            val overallBudget = _uiState.value.overallBudgetInput.toDoubleOrNull() ?: 0.0
            val categoryBudgets = _uiState.value.categoryBudgetsInput.mapValues { it.value.toDoubleOrNull() ?: 0.0 }
            val newBudget = Budget(
                id = _uiState.value.budget?.id ?: 0,
                month = _uiState.value.selectedMonth,
                year = _uiState.value.selectedYear,
                overallBudget = overallBudget,
                categoryBudgets = categoryBudgets
            )
            budgetRepository.insertOrUpdateBudget(newBudget)
        }
    }

    fun addCategory(categoryName: String) {
        viewModelScope.launch {
            budgetRepository.insertCategory(Category(name = categoryName))
            _uiState.value = _uiState.value.copy(
                allCategories = budgetRepository.getAllCategories().first()
            )
        }
    }
} 