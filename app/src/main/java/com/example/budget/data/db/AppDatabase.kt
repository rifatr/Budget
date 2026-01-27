package com.example.budget.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(entities = [Category::class, Expense::class, Budget::class], version = 2, exportSchema = false)
@TypeConverters(DateConverter::class, CategoryBudgetConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN usageCount INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Callback to populate initial categories on database creation
        private class AppDatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    // Use a coroutine to populate the database asynchronously
                    CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                        populateInitialCategories(database.categoryDao())
                    }
                }
            }
        }

        private suspend fun populateInitialCategories(categoryDao: CategoryDao) {
            // Insert initial categories: Food, Transport, Shopping
            categoryDao.insertCategory(Category(name = "Food"))
            categoryDao.insertCategory(Category(name = "Transport"))
            categoryDao.insertCategory(Category(name = "Shopping"))
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_database"
                )
                .addMigrations(MIGRATION_1_2)
                .addCallback(AppDatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 