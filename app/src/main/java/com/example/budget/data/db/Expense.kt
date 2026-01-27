package com.example.budget.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import com.example.budget.data.db.Category

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Date,
    val categoryId: Int,
    val amount: Double,
    val description: String?
)

/**
 * Common sorting for expenses: Date descending, then ID descending.
 * This ensures most recent expenses (including those added same-day) appear first.
 */
fun List<Expense>.sortedByDateAndId(): List<Expense> = 
    this.sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.id }) 