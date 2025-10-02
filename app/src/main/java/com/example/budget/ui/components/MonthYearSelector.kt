package com.example.budget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.budget.data.DateConstants

@Composable
fun MonthYearSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val months = DateConstants.MONTHS
    val years = DateConstants.getAvailableYears()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month Selector
        Box(modifier = Modifier.weight(1f)) {
            BeautifulSelector(
                label = "Month",
                value = DateConstants.getMonthName(selectedMonth),
                options = months.map { it.first },
                onSelectionChange = { selectedMonthName ->
                    onMonthChange(DateConstants.getMonthNumber(selectedMonthName))
                }
            )
        }

        // Year Selector
        Box(modifier = Modifier.weight(1f)) {
            BeautifulSelector(
                label = "Year",
                value = selectedYear.toString(),
                options = years.map { it.toString() },
                onSelectionChange = { selectedYearString ->
                    onYearChange(selectedYearString.toInt())
                }
            )
        }
    }
}
