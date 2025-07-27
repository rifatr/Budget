package com.example.budget.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CategoryBudgetConverter {
    @TypeConverter
    fun fromCategoryBudgetMap(categoryBudgets: Map<Int, Double>): String {
        return Gson().toJson(categoryBudgets)
    }

    @TypeConverter
    fun toCategoryBudgetMap(json: String): Map<Int, Double> {
        val type = object : TypeToken<Map<Int, Double>>() {}.type
        return Gson().fromJson(json, type)
    }
} 