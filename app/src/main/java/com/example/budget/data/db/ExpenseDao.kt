package com.example.budget.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getExpensesForMonth(startDate: Date, endDate: Date): Flow<List<Expense>>

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpensesFlow(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId")
    suspend fun getExpensesByCategory(categoryId: Int): List<Expense>

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId")
    suspend fun getExpenseCountByCategory(categoryId: Int): Int

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE categoryId = :categoryId")
    suspend fun getTotalAmountByCategory(categoryId: Int): Double

    @Query("DELETE FROM expenses WHERE categoryId = :categoryId")
    suspend fun deleteExpensesByCategory(categoryId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
    
    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Int)

    @Query("DELETE FROM expenses")
    suspend fun clearAll()
} 