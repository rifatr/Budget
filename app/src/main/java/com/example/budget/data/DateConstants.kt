package com.example.budget.data

import java.util.Calendar
import java.util.Date

object DateConstants {
    // Fixed year range for data consistency and accessibility
    private const val MIN_YEAR = 2020
    private const val MAX_YEAR = 2080
    
    // Month names mapping
    val MONTHS = listOf(
        "January" to 1, "February" to 2, "March" to 3, "April" to 4,
        "May" to 5, "June" to 6, "July" to 7, "August" to 8,
        "September" to 9, "October" to 10, "November" to 11, "December" to 12
    )
    
    // Helper functions
    fun getAvailableYears(): List<Int> {
        return (MIN_YEAR..MAX_YEAR).toList()
    }
    
    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    
    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
    
    /**
     * Gets start and end timestamps for filtering expenses within a specific month.
     * Returns the first millisecond of the month (00:00:00.000) to the last millisecond (23:59:59.999).
     * Used by repository queries to get all expenses that occurred within the month boundaries.
     * 
     * @param year The year
     * @param month The month (1-12)
     * @return Pair of (monthStartTimestamp, monthEndTimestamp) for database filtering
     */
    fun getMonthStartAndEndTimestamps(year: Int, month: Int): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        
        // Set to first millisecond of the month
        calendar.set(year, month - 1, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val monthStartTimestamp = calendar.time
        
        // Set to last millisecond of the month
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val monthEndTimestamp = calendar.time
        
        return monthStartTimestamp to monthEndTimestamp
    }
    
    /**
     * Get month name from month number.
     * 
     * @param month The month number (1-12, where 1 = January)
     * @return Month name as String (e.g., "January", "February", etc.)
     */
    fun getMonthName(month: Int): String {
        return MONTHS.find { it.second == month }?.first ?: "Unknown"
    }
    
    /**
     * Get month number from month name.
     * 
     * @param monthName The month name (e.g., "January", "February", etc.)
     * @return Month number (1-12, where 1 = January)
     */
    fun getMonthNumber(monthName: String): Int {
        return MONTHS.find { it.first == monthName }?.second ?: 1
    }
    
    /**
     * Get month name and year from a Date object.
     * 
     * @param date The Date object
     * @return Formatted string like "January 2024"
     */
    fun getMonthYearString(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return "${getMonthName(month)} $year"
    }
}
