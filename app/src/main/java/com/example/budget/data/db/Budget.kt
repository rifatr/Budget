package com.example.budget.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val month: Int, // 1-12
    val year: Int,
    val overallBudget: Double,
    val categoryBudgets: Map<Int, Double> // CategoryId to Budgeted Amount
) 