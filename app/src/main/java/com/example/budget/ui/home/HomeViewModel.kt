package com.example.budget.ui.home

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

data class HomeUiState(
    val selectedMonth: Int,
    val selectedYear: Int,
    val budget: Budget?,
    val expenses: List<Expense>,
    val categories: List<Category>,
    val summary: Map<Category, Double>
)

class HomeViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1,
            selectedYear = Calendar.getInstance().get(Calendar.YEAR),
            budget = null,
            expenses = emptyList(),
            categories = emptyList(),
            summary = emptyMap()
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val (startDate, endDate) = getMonthDateRange(uiState.value.selectedYear, uiState.value.selectedMonth)
            combine(
                budgetRepository.getBudgetForMonth(uiState.value.selectedMonth, uiState.value.selectedYear),
                budgetRepository.getExpensesForMonth(startDate, endDate),
                budgetRepository.getAllCategories()
            ) { budget, expenses, categories ->
                val summary = categories.associateWith { category ->
                    expenses.filter { it.categoryId == category.id }.sumOf { it.amount }
                }
                HomeUiState(
                    selectedMonth = uiState.value.selectedMonth,
                    selectedYear = uiState.value.selectedYear,
                    budget = budget,
                    expenses = expenses,
                    categories = categories,
                    summary = summary
                )
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun onDateChange(month: Int, year: Int) {
        viewModelScope.launch {
            val (startDate, endDate) = getMonthDateRange(year, month)
            combine(
                budgetRepository.getBudgetForMonth(month, year),
                budgetRepository.getExpensesForMonth(startDate, endDate),
                budgetRepository.getAllCategories()
            ) { budget, expenses, categories ->
                val summary = categories.associateWith { category ->
                    expenses.filter { it.categoryId == category.id }.sumOf { it.amount }
                }
                HomeUiState(
                    selectedMonth = month,
                    selectedYear = year,
                    budget = budget,
                    expenses = expenses,
                    categories = categories,
                    summary = summary
                )
            }.collect {
                _uiState.value = it
            }
        }
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