package com.example.budget.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY usageCount DESC, name ASC")
    fun getAllCategoriesByUsage(): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesList(): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("UPDATE categories SET usageCount = usageCount + 1 WHERE id = :categoryId")
    suspend fun incrementUsageCount(categoryId: Int)

    @Query("UPDATE categories SET name = :newName WHERE id = :categoryId")
    suspend fun updateCategoryName(categoryId: Int, newName: String)

    @Query("DELETE FROM categories")
    suspend fun clearAll()
} 