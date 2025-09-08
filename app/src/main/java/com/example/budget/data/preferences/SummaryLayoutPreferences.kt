package com.example.budget.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SummaryLayoutType {
    CARDS, TABLE
}

class SummaryLayoutPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _summaryLayoutType = MutableStateFlow(getSummaryLayoutType())
    val summaryLayoutType: StateFlow<SummaryLayoutType> = _summaryLayoutType.asStateFlow()
    
    fun setSummaryLayoutType(layoutType: SummaryLayoutType) {
        prefs.edit().putString(KEY_SUMMARY_LAYOUT, layoutType.name).apply()
        _summaryLayoutType.value = layoutType
    }
    
    private fun getSummaryLayoutType(): SummaryLayoutType {
        val layoutName = prefs.getString(KEY_SUMMARY_LAYOUT, SummaryLayoutType.CARDS.name) ?: SummaryLayoutType.CARDS.name
        return try {
            SummaryLayoutType.valueOf(layoutName)
        } catch (e: IllegalArgumentException) {
            SummaryLayoutType.CARDS
        }
    }
    
    companion object {
        private const val PREFS_NAME = "summary_layout_preferences"
        private const val KEY_SUMMARY_LAYOUT = "summary_layout_type"
    }
}
