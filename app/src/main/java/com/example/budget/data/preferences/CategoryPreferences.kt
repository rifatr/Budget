package com.example.budget.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategoryPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _lastSelectedCategoryId = MutableStateFlow(getLastSelectedCategoryId())
    val lastSelectedCategoryId: StateFlow<Int?> = _lastSelectedCategoryId.asStateFlow()
    
    fun setLastSelectedCategoryId(categoryId: Int?) {
        if (categoryId != null) {
            prefs.edit().putInt(KEY_LAST_SELECTED_CATEGORY, categoryId).apply()
            _lastSelectedCategoryId.value = categoryId
        } else {
            prefs.edit().remove(KEY_LAST_SELECTED_CATEGORY).apply()
            _lastSelectedCategoryId.value = null
        }
    }
    
    private fun getLastSelectedCategoryId(): Int? {
        return if (prefs.contains(KEY_LAST_SELECTED_CATEGORY)) {
            prefs.getInt(KEY_LAST_SELECTED_CATEGORY, -1).takeIf { it != -1 }
        } else {
            null
        }
    }
    
    companion object {
        private const val PREFS_NAME = "category_preferences"
        private const val KEY_LAST_SELECTED_CATEGORY = "last_selected_category_id"
    }
}
