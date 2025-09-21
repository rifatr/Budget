package com.example.budget.data

/**
 * Centralized validation constants for the Budget application.
 * Contains all input validation limits and patterns used across the app.
 */
object ValidationConstants {
    // Text length limits
    const val CATEGORY_NAME_MAX_LENGTH = 24
    const val EXPENSE_DESCRIPTION_MAX_LENGTH = 150
    
    // Amount validation
    private const val AMOUNT_DIGITS_BEFORE_DECIMAL = 8
    private const val AMOUNT_DIGITS_AFTER_DECIMAL = 2
    
    // Generate regex pattern for amount validation
    val AMOUNT_VALIDATION_REGEX = Regex("^\\d{0,$AMOUNT_DIGITS_BEFORE_DECIMAL}(\\.\\d{0,$AMOUNT_DIGITS_AFTER_DECIMAL})?\$")
}

