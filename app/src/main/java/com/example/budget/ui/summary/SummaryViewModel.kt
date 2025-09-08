package com.example.budget.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetRepository
import com.example.budget.data.preferences.SummaryLayoutPreferences
import com.example.budget.data.preferences.SummaryLayoutType
import com.example.budget.data.db.Budget
import com.example.budget.data.db.Category
import com.example.budget.data.db.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

enum class SortOption {
    NAME_ASC, NAME_DESC, SPENT_ASC, SPENT_DESC, BUDGET_ASC, BUDGET_DESC, REMAINING_ASC, REMAINING_DESC
}

data class SummaryUiState(
    val selectedMonth: Int,
    val selectedYear: Int,
    val budget: Budget?,
    val expenses: List<Expense>,
    val categories: List<Category>,
    val summary: Map<Category, SummaryRow>,
    val isLoading: Boolean = false,
    val summaryRows: List<SummaryRow> = emptyList(),
    val sortedSummaryRows: List<SummaryRow> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val currentSort: SortOption = SortOption.NAME_ASC,
    val layoutType: SummaryLayoutType = SummaryLayoutType.CARDS
)

data class SummaryRow(
    val category: Category,
    val budgeted: Double,
    val actual: Double,
    val delta: Double
)

class SummaryViewModel(
    private val budgetRepository: BudgetRepository,
    private val summaryLayoutPreferences: SummaryLayoutPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SummaryUiState(
            selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1,
            selectedYear = Calendar.getInstance().get(Calendar.YEAR),
            budget = null,
            expenses = emptyList(),
            categories = emptyList(),
            summary = emptyMap(),
            isLoading = true
        )
    )
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    fun initialize(month: Int, year: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val (startDate, endDate) = getMonthDateRange(year, month)
            combine(
                budgetRepository.getBudgetForMonth(month, year),
                budgetRepository.getExpensesForMonth(startDate, endDate),
                budgetRepository.getAllCategories(),
                summaryLayoutPreferences.summaryLayoutType
            ) { budget, expenses, categories, layoutType ->
                val summary = categories.associateWith { category ->
                    val budgeted = budget?.categoryBudgets?.get(category.id) ?: 0.0
                    val actual = expenses.filter { it.categoryId == category.id }.sumOf { it.amount }
                    SummaryRow(
                        category = category,
                        budgeted = budgeted,
                        actual = actual,
                        delta = budgeted - actual
                    )
                }
                
                val summaryRows = summary.values.toList()
                val totalBudget = budget?.overallBudget ?: 0.0
                val totalSpent = expenses.sumOf { it.amount }
                
                val currentState = _uiState.value
                SummaryUiState(
                    selectedMonth = month,
                    selectedYear = year,
                    budget = budget,
                    expenses = expenses,
                    categories = categories,
                    summary = summary,
                    isLoading = false,
                    summaryRows = summaryRows,
                    sortedSummaryRows = summaryRows, // Will be updated by applySorting
                    totalBudget = totalBudget,
                    totalSpent = totalSpent,
                    currentSort = currentState.currentSort,
                    layoutType = layoutType
                )
            }.collect {
                _uiState.value = it
                applySorting()
            }
        }
    }
    
    fun updateSort(sortOption: SortOption) {
        _uiState.value = _uiState.value.copy(currentSort = sortOption)
        applySorting()
    }
    
    private fun applySorting() {
        val currentState = _uiState.value
        val sortedRows = when (currentState.currentSort) {
            SortOption.NAME_ASC -> currentState.summaryRows.sortedBy { it.category.name.lowercase() }
            SortOption.NAME_DESC -> currentState.summaryRows.sortedByDescending { it.category.name.lowercase() }
            SortOption.SPENT_ASC -> currentState.summaryRows.sortedBy { it.actual }
            SortOption.SPENT_DESC -> currentState.summaryRows.sortedByDescending { it.actual }
            SortOption.BUDGET_ASC -> currentState.summaryRows.sortedBy { it.budgeted }
            SortOption.BUDGET_DESC -> currentState.summaryRows.sortedByDescending { it.budgeted }
            SortOption.REMAINING_ASC -> currentState.summaryRows.sortedBy { it.budgeted - it.actual }
            SortOption.REMAINING_DESC -> currentState.summaryRows.sortedByDescending { it.budgeted - it.actual }
        }
        
        _uiState.value = _uiState.value.copy(sortedSummaryRows = sortedRows)
    }

    private fun getMonthDateRange(year: Int, month: Int): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        val startDate = calendar.time
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.time
        return startDate to endDate
    }
} 