package com.example.budget.ui.summary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.budget.ui.AppViewModelProvider
import com.example.budget.ui.Screen
import com.example.budget.ui.components.BeautifulSelector
import com.example.budget.ui.utils.formatCurrency
import com.example.budget.ui.utils.getDynamicTextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    navController: NavController = rememberNavController(),
    viewModel: SummaryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val app = context.applicationContext as com.example.budget.BudgetApp
    val selectedCurrency by app.container.currencyPreferences.selectedCurrency.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    // Current month/year state
    val calendar = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }

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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OverallSummaryCard(
                            totalBudget = uiState.totalBudget,
                            totalSpent = uiState.totalSpent,
                            currencySymbol = selectedCurrency.symbol
                        )
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Categories",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            SortButton(
                                currentSort = uiState.currentSort,
                                onSortChange = viewModel::updateSort
                            )
                        }
                    }

                    items(uiState.sortedSummaryRows) { row ->
                        CategoryCard(
                            row = row,
                            currencySymbol = selectedCurrency.symbol,
                            onClick = {
                                navController.navigate(
                                    "${Screen.CategoryExpenseDetail.route}/${row.category.id}/${row.category.name}/${selectedMonth}/${selectedYear}"
                                )
                            }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
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
    val months = listOf(
        "January" to 1, "February" to 2, "March" to 3, "April" to 4,
        "May" to 5, "June" to 6, "July" to 7, "August" to 8,
        "September" to 9, "October" to 10, "November" to 11, "December" to 12
    )
    
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 5..currentYear + 5).toList()

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



@Composable
fun OverallSummaryCard(
    totalBudget: Double,
    totalSpent: Double,
    currencySymbol: String
) {
    val progress = if (totalBudget > 0) (totalSpent / totalBudget).coerceAtMost(1.0).toFloat() else 0f
    val isOverBudget = totalSpent > totalBudget && totalBudget > 0
    val remaining = totalBudget - totalSpent
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget) 
                MaterialTheme.colorScheme.errorContainer 
            else 
                MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Use the larger amount to determine consistent font size for both
                val consistentAmountTextStyle = getDynamicTextStyle(maxOf(totalSpent, totalBudget), currencySymbol)
                
                Column {
                    Text(
                        text = "Total Spent",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = formatCurrency(totalSpent, currencySymbol),
                        style = consistentAmountTextStyle,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                if (totalBudget > 0) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Budget",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = formatCurrency(totalBudget, currencySymbol),
                            style = consistentAmountTextStyle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            if (totalBudget > 0) {
                Spacer(modifier = Modifier.height(20.dp))
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isOverBudget) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = if (isOverBudget) 
                        "Over budget by ${formatCurrency(-remaining, currencySymbol)}"
                    else 
                        "Remaining: ${formatCurrency(remaining, currencySymbol)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (isOverBudget) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    row: SummaryRow,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val progress = if (row.budgeted > 0) (row.actual / row.budgeted).coerceAtMost(1.0).toFloat() else 0f
    val isOverBudget = row.actual > row.budgeted && row.budgeted > 0
    val statusColor = when {
        row.budgeted == 0.0 -> MaterialTheme.colorScheme.outline
        isOverBudget -> MaterialTheme.colorScheme.error
        row.actual == 0.0 -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.primary
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = row.category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Show remaining budget - the key requirement!
                    val remaining = row.budgeted - row.actual
                    Text(
                        text = when {
                            row.budgeted == 0.0 -> "No budget set"
                            row.actual == 0.0 -> "No expenses yet"
                            isOverBudget -> "${formatCurrency(-remaining, currencySymbol)} over budget"
                            else -> "${formatCurrency(remaining, currencySymbol)} remaining"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = statusColor
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatCurrency(row.actual, currencySymbol),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (row.budgeted > 0) {
                        Text(
                            text = "of ${formatCurrency(row.budgeted, currencySymbol)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            if (row.budgeted > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun SortButton(
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
    ) {
        OutlinedButton(
            onClick = { showSortMenu = true }
        ) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
            Spacer(modifier = Modifier.width(4.dp))
            Text(getSortDisplayName(currentSort))
        }
        
        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false }
        ) {
            SortOption.entries.forEach { sortOption ->
                DropdownMenuItem(
                    text = { Text(getSortDisplayName(sortOption)) },
                    onClick = {
                        onSortChange(sortOption)
                        showSortMenu = false
                    }
                )
            }
        }
    }
}

private fun getSortDisplayName(sortOption: SortOption): String {
    return when (sortOption) {
        SortOption.NAME_ASC -> "Name ↑"
        SortOption.NAME_DESC -> "Name ↓"
        SortOption.SPENT_ASC -> "Spent ↑"
        SortOption.SPENT_DESC -> "Spent ↓"
        SortOption.BUDGET_ASC -> "Budget ↑"
        SortOption.BUDGET_DESC -> "Budget ↓"
        SortOption.REMAINING_ASC -> "Remaining ↑"
        SortOption.REMAINING_DESC -> "Remaining ↓"
    }
} 