package com.example.budget.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.budget.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: SummaryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val app = context.applicationContext as com.example.budget.BudgetApp
    val selectedCurrency by app.container.currencyPreferences.selectedCurrency.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    // Current month/year state
    val calendar = Calendar.getInstance()
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }

    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.initialize(selectedMonth, selectedYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Month/Year Selector
            MonthYearSelector(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthChange = { selectedMonth = it },
                onYearChange = { selectedYear = it },
                modifier = Modifier.padding(16.dp)
            )
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        SummaryHeader()
                    }

                    items(uiState.summaryRows) { row ->
                        SummaryRow(row = row, currencySymbol = selectedCurrency.symbol)
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SummaryTotals(uiState = uiState, currencySymbol = selectedCurrency.symbol)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthYearSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMonthDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }
    
    val months = (1..12).map { month ->
        val calendar = Calendar.getInstance().apply { set(Calendar.MONTH, month - 1) }
        SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.time) to month
    }
    
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 5..currentYear + 5).toList()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month Selector
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = { showMonthDropdown = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(months.find { it.second == selectedMonth }?.first ?: "")
                Icon(
                    if (showMonthDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            
            DropdownMenu(
                expanded = showMonthDropdown,
                onDismissRequest = { showMonthDropdown = false }
            ) {
                months.forEach { (monthName, monthNumber) ->
                    DropdownMenuItem(
                        text = { Text(monthName) },
                        onClick = {
                            onMonthChange(monthNumber)
                            showMonthDropdown = false
                        }
                    )
                }
            }
        }
        
        // Year Selector
        Box(modifier = Modifier.weight(1f)) {
            OutlinedButton(
                onClick = { showYearDropdown = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedYear.toString())
                Icon(
                    if (showYearDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            
            DropdownMenu(
                expanded = showYearDropdown,
                onDismissRequest = { showYearDropdown = false }
            ) {
                years.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = {
                            onYearChange(year)
                            showYearDropdown = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
        Text("Budgeted", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        Text("Expense", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        Text("Delta", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
    }
}

@Composable
fun SummaryRow(row: SummaryRow, currencySymbol: String) {
    val positiveDeltaColor = Color(0xFF388E3C) // A more subtle green
    val negativeDeltaColor = MaterialTheme.colorScheme.error
    val progress = if (row.budgeted > 0) (row.actual / row.budgeted).toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.category.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$currencySymbol${String.format("%.2f", row.budgeted)}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, maxLines = 1)
                Text("$currencySymbol${String.format("%.2f", row.actual)}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, maxLines = 1)
                Text(
                    text = "$currencySymbol${String.format("%.2f", row.delta)}",
                    color = if (row.delta >= 0) positiveDeltaColor else negativeDeltaColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f),
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
            if (row.budgeted > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (progress > 1f) negativeDeltaColor else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun SummaryTotals(uiState: SummaryUiState, currencySymbol: String) {
    val totalBudgeted = uiState.summary.values.sumOf { it.budgeted }
    val totalActual = uiState.summary.values.sumOf { it.actual }
    val totalDelta = totalBudgeted - totalActual
    val positiveDeltaColor = Color(0xFF388E3C)
    val negativeDeltaColor = MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Totals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
        Text("$currencySymbol${String.format("%.2f", totalBudgeted)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, maxLines = 1)
        Text("$currencySymbol${String.format("%.2f", totalActual)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, maxLines = 1)
        Text(
            text = "$currencySymbol${String.format("%.2f", totalDelta)}",
            color = if (totalDelta >= 0) positiveDeltaColor else negativeDeltaColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
} 