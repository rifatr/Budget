package com.example.budget.ui.expensehistory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budget.data.db.Expense
import com.example.budget.ui.AppViewModelProvider
import com.example.budget.ui.components.ConfirmationMessage
import com.example.budget.ui.components.BeautifulSelector
import com.example.budget.ui.utils.formatCurrency
import com.example.budget.ui.utils.getDynamicTextStyle
import com.example.budget.data.DateConstants
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExpenseHistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val app = context.applicationContext as com.example.budget.BudgetApp
    val selectedCurrency by app.container.currencyPreferences.selectedCurrency.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            if (uiState.showConfirmationMessage) {
                ConfirmationMessage(
                    message = uiState.confirmationMessage,
                    isError = uiState.isConfirmationError,
                    onDismiss = { viewModel.dismissConfirmationMessage() }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // Month/Year selector
                MonthYearSelector(
                    selectedMonth = uiState.selectedMonth,
                    selectedYear = uiState.selectedYear,
                    onMonthChange = { month -> viewModel.updateMonthYear(month, uiState.selectedYear) },
                    onYearChange = { year -> viewModel.updateMonthYear(uiState.selectedMonth, year) }
                )
            }
            
            item {
                Text(
                    text = when {
                        uiState.expenses.isEmpty() -> "No expenses yet"
                        uiState.expenses.size == 1 -> "1 expense"
                        else -> "${uiState.expenses.size} expenses"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (uiState.expenses.isNotEmpty()) {
                items(uiState.expenses) { expense ->
                    ExpenseHistoryItem(
                        expense = expense,
                        categoryMap = uiState.categoryMap,
                        currencySymbol = selectedCurrency.symbol,
                        onDeleteExpense = { 
                            expenseToDelete = it
                            showDeleteConfirmation = true
                        }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteConfirmation = false
                expenseToDelete = null
            },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseToDelete?.let { viewModel.deleteExpense(it) }
                        showDeleteConfirmation = false
                        expenseToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirmation = false
                        expenseToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun ExpenseHistoryItem(
    expense: Expense,
    categoryMap: Map<Int, String>,
    currencySymbol: String,
    onDeleteExpense: (Expense) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = categoryMap[expense.categoryId] ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!expense.description.isNullOrBlank()) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatCurrency(expense.amount, currencySymbol),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = dateFormat.format(expense.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = { onDeleteExpense(expense) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

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
                value = months.find { it.second == selectedMonth }?.first ?: "January",
                options = months.map { it.first },
                onSelectionChange = { selectedMonthName ->
                    val monthValue = months.find { it.first == selectedMonthName }?.second ?: 1
                    onMonthChange(monthValue)
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
