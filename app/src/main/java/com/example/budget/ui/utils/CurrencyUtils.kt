package com.example.budget.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import java.text.NumberFormat
import java.util.*

// Helper function to format numbers with commas for readability
fun formatNumberWithCommas(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US)
    formatter.minimumFractionDigits = 2
    formatter.maximumFractionDigits = 2
    return formatter.format(amount)
}

// Helper function to format currency with symbol and commas
fun formatCurrency(amount: Double, currencySymbol: String): String {
    return "$currencySymbol${formatNumberWithCommas(amount)}"
}

// Helper function to get dynamic text style based on amount size
@Composable
fun getDynamicTextStyle(amount: Double, currencySymbol: String): TextStyle {
    val formattedLength = formatCurrency(amount, currencySymbol).length
    return when {
        formattedLength <= 8  -> MaterialTheme.typography.headlineLarge    // $123.00
        formattedLength <= 10 -> MaterialTheme.typography.headlineMedium   // $1,234.00
        formattedLength <= 12 -> MaterialTheme.typography.headlineSmall    // $12,345.00
        formattedLength <= 14 -> MaterialTheme.typography.titleLarge       // $123,456.00
        formattedLength <= 16 -> MaterialTheme.typography.titleMedium      // $1,234,567.00
        else -> MaterialTheme.typography.titleSmall                        // $12,345,678.00+
    }
}
