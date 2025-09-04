package com.example.budget.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetRepository
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
    val filteredSummaryRows: List<SummaryRow> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val searchQuery: String = "",
    val currentSort: SortOption = SortOption.NAME_ASC
)

data class SummaryRow(
    val category: Category,
    val budgeted: Double,
    val actual: Double,
    val delta: Double
)

class SummaryViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

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
                budgetRepository.getAllCategories()
            ) { budget, expenses, categories ->
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
                    filteredSummaryRows = summaryRows, // Will be updated by applyFiltersAndSort
                    totalBudget = totalBudget,
                    totalSpent = totalSpent,
                    searchQuery = currentState.searchQuery,
                    currentSort = currentState.currentSort
                )
            }.collect {
                _uiState.value = it
                applyFiltersAndSort()
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFiltersAndSort()
    }
    
    fun updateSort(sortOption: SortOption) {
        _uiState.value = _uiState.value.copy(currentSort = sortOption)
        applyFiltersAndSort()
    }
    
    private fun applyFiltersAndSort() {
        val currentState = _uiState.value
        var filteredRows = currentState.summaryRows
        
        // Apply search filter
        if (currentState.searchQuery.isNotBlank()) {
            filteredRows = filteredRows.filter { row ->
                row.category.name.contains(currentState.searchQuery, ignoreCase = true)
            }
        }
        
        // Apply sort
        filteredRows = when (currentState.currentSort) {
            SortOption.NAME_ASC -> filteredRows.sortedBy { it.category.name.lowercase() }
            SortOption.NAME_DESC -> filteredRows.sortedByDescending { it.category.name.lowercase() }
            SortOption.SPENT_ASC -> filteredRows.sortedBy { it.actual }
            SortOption.SPENT_DESC -> filteredRows.sortedByDescending { it.actual }
            SortOption.BUDGET_ASC -> filteredRows.sortedBy { it.budgeted }
            SortOption.BUDGET_DESC -> filteredRows.sortedByDescending { it.budgeted }
            SortOption.REMAINING_ASC -> filteredRows.sortedBy { it.budgeted - it.actual }
            SortOption.REMAINING_DESC -> filteredRows.sortedByDescending { it.budgeted - it.actual }
        }
        
        _uiState.value = _uiState.value.copy(filteredSummaryRows = filteredRows)
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