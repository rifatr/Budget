package com.example.budget.data

import com.example.budget.data.db.Budget
import com.example.budget.data.db.BudgetDao
import com.example.budget.data.db.Category
import com.example.budget.data.db.CategoryDao
import com.example.budget.data.db.Expense
import com.example.budget.data.db.ExpenseDao
import kotlinx.coroutines.flow.Flow
import java.util.Date

class BudgetRepository(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao
) {

    // Category operations
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    fun getAllCategoriesByUsage(): Flow<List<Category>> = categoryDao.getAllCategoriesByUsage()
    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)
    suspend fun updateCategoryName(categoryId: Int, newName: String) = categoryDao.updateCategoryName(categoryId, newName)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
    suspend fun incrementCategoryUsage(categoryId: Int) = categoryDao.incrementUsageCount(categoryId)
    
    // Category statistics
    suspend fun getExpenseCountByCategory(categoryId: Int): Int = expenseDao.getExpenseCountByCategory(categoryId)
    suspend fun getTotalAmountByCategory(categoryId: Int): Double = expenseDao.getTotalAmountByCategory(categoryId)
    suspend fun deleteExpensesByCategory(categoryId: Int) = expenseDao.deleteExpensesByCategory(categoryId)

    // Expense operations
    fun getExpensesForMonth(startDate: Date, endDate: Date): Flow<List<Expense>> =
        expenseDao.getExpensesForMonth(startDate, endDate)
    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpensesFlow()
    suspend fun insertExpense(expense: Expense) = expenseDao.insertExpense(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)

    // Budget operations
    fun getBudgetForMonth(month: Int, year: Int): Flow<Budget?> =
        budgetDao.getBudgetForMonth(month, year)
    suspend fun insertOrUpdateBudget(budget: Budget) = budgetDao.insertOrUpdateBudget(budget)

    // Export/Import
    suspend fun getBudgetData(): BudgetData {
        return BudgetData(
            categories = categoryDao.getAllCategoriesList(),
            expenses = expenseDao.getAllExpenses(),
            budgets = budgetDao.getAllBudgets()
        )
    }

    suspend fun importBudgetData(budgetData: BudgetData) {
        categoryDao.clearAll()
        expenseDao.clearAll()
        budgetDao.clearAll()
        budgetData.categories.forEach { categoryDao.insertCategory(it) }
        budgetData.expenses.forEach { expenseDao.insertExpense(it) }
        budgetData.budgets.forEach { budgetDao.insertOrUpdateBudget(it) }
    }
} 