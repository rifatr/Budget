package com.example.budget.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CurrencyPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _selectedCurrency = MutableStateFlow(getSelectedCurrency())
    val selectedCurrency: StateFlow<Currency> = _selectedCurrency.asStateFlow()
    
    private val _isFirstLaunch = MutableStateFlow(isFirstLaunch())
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()
    
    fun setSelectedCurrency(currency: Currency) {
        prefs.edit().putString(KEY_CURRENCY, currency.code).apply()
        _selectedCurrency.value = currency
    }
    
    fun markFirstLaunchComplete() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        _isFirstLaunch.value = false
    }
    
    private fun getSelectedCurrency(): Currency {
        val currencyCode = prefs.getString(KEY_CURRENCY, Currency.DOLLAR.code) ?: Currency.DOLLAR.code
        return Currency.fromCode(currencyCode)
    }
    
    private fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    companion object {
        private const val PREFS_NAME = "budget_preferences"
        private const val KEY_CURRENCY = "selected_currency"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }
} 