package com.example.budget.ui.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.budget.data.db.Category
import com.example.budget.data.db.Expense
import com.example.budget.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.unit.DpOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showExpenseHistory() }) {
                        Icon(Icons.Default.History, contentDescription = "Expense History")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.isEntryValid) {
                        viewModel.saveExpense()
                        navController.popBackStack()
                    }
                },
                containerColor = if (uiState.isEntryValid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(Icons.Default.Done, contentDescription = "Save Expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DateSelector(
                date = uiState.date,
                onDateChange = { viewModel.onDateChange(it) }
            )
            CategorySelector(
                categories = uiState.allCategories,
                selectedCategory = uiState.category,
                onCategoryChange = { viewModel.onCategoryChange(it) }
            )
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { viewModel.onAmountChange(it) },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.amount.toDoubleOrNull() == null && uiState.amount.isNotBlank(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                label = { Text("Description (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Expense History Dialog
    if (uiState.showHistory) {
        ExpenseHistoryDialog(
            expenses = uiState.expenseHistory,
            categoryMap = uiState.categoryMap,
            onDismiss = { viewModel.hideExpenseHistory() },
            onDeleteExpense = { viewModel.deleteExpense(it) }
        )
    }
}

@Composable
fun DateSelector(date: Date, onDateChange: (Date) -> Unit) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val calendar = Calendar.getInstance()
    calendar.time = date

    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newDate = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }.time
            onDateChange(newDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Button(onClick = { datePickerDialog.show() }) {
        Text("Date: ${dateFormat.format(date)}")
    }
}

@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategoryChange: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCategory?.name ?: "Select Category",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, 4.dp),
            modifier = Modifier
                .heightIn(max = 200.dp)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (category == selectedCategory) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    onClick = {
                        onCategoryChange(category)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            }
        }
    }
}

@Composable
fun ExpenseHistoryDialog(
    expenses: List<Expense>,
    categoryMap: Map<Int, String>,
    onDismiss: () -> Unit,
    onDeleteExpense: (Expense) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Expense History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (expenses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No expenses recorded yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(expenses) { expense ->
                            ExpenseHistoryItem(
                                expense = expense,
                                categoryMap = categoryMap,
                                onDeleteExpense = onDeleteExpense
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseHistoryItem(
    expense: Expense,
    categoryMap: Map<Int, String>,
    onDeleteExpense: (Expense) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Category: ${categoryMap[expense.categoryId] ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Amount: $${String.format("%.2f", expense.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = dateFormat.format(expense.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!expense.description.isNullOrBlank()) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            IconButton(
                onClick = { onDeleteExpense(expense) },
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete expense"
                )
            }
        }
    }
} 