package com.example.budget.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budget.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val app = context.applicationContext as com.example.budget.BudgetApp
    val selectedCurrency by app.container.currencyPreferences.selectedCurrency.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    // Keyboard and focus management
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    
    // Current month/year state
    val calendar = Calendar.getInstance()
    var selectedMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryBudget by remember { mutableStateOf("") }
    var showDuplicateError by remember { mutableStateOf(false) }

    LaunchedEffect(selectedMonth, selectedYear) {
        viewModel.initialize(selectedMonth, selectedYear)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    })
                }
        ) {
            // Month/Year Selector
            MonthYearSelector(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onMonthChange = { selectedMonth = it },
                onYearChange = { selectedYear = it },
                modifier = Modifier.padding(16.dp)
            )
            
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Total Budget",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = uiState.totalBudgetInput,
                                onValueChange = viewModel::updateTotalBudgetInput,
                                label = { Text("Monthly Budget") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        viewModel.saveBudget()
                                    }
                                ),
                                leadingIcon = {
                                    Text(
                                        text = selectedCurrency.symbol,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        viewModel.saveBudget()
                                    }) {
                                        Icon(Icons.Default.Done, contentDescription = "Save")
                                    }
                                }
                            )
                        }
                    }
                }

                // Budget Summary Section
                if (uiState.hasTotalBudget) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.uncategorizedBudget >= 0) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Categorized:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${selectedCurrency.symbol}${String.format("%.2f", uiState.totalCategorizedBudget)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (uiState.uncategorizedBudget >= 0) "Remaining:" else "Over budget:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.uncategorizedBudget >= 0) 
                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "${selectedCurrency.symbol}${String.format("%.2f", kotlin.math.abs(uiState.uncategorizedBudget))}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.uncategorizedBudget >= 0) 
                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(uiState.allCategories) { category ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier.width(180.dp)
                            ) {
                                OutlinedTextField(
                                    value = uiState.categoryBudgets[category.id]?.toString() ?: "",
                                    onValueChange = { value ->
                                        viewModel.updateCategoryBudget(category.id, value)
                                    },
                                    label = { Text("Budget") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                            viewModel.saveCategoryBudget(category.id)
                                        }
                                    ),
                                    leadingIcon = {
                                        Text(
                                            text = selectedCurrency.symbol,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                IconButton(
                                    onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        viewModel.saveCategoryBudget(category.id)
                                    },
                                    modifier = Modifier.align(Alignment.CenterEnd)
                                ) {
                                    Icon(Icons.Default.Done, contentDescription = "Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Temporary test button to verify messages work - place it at the top
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = { viewModel.testMessage() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Test Message")
        }
    }

    // Success Message Snackbar
    if (uiState.showSuccessMessage) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // Add bottom padding to avoid overlap with FAB
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.successMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { viewModel.dismissSuccessMessage() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddCategoryDialog = false
                newCategoryName = ""
                newCategoryBudget = ""
                showDuplicateError = false
            },
            title = { Text("Add Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { 
                            // Limit category name to specified length
                            if (it.length <= ValidationConstants.CATEGORY_NAME_MAX_LENGTH) {
                                newCategoryName = it
                                showDuplicateError = false
                            }
                        },
                        label = { Text("Category Name") },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        isError = showDuplicateError,
                        supportingText = if (showDuplicateError) {
                            { Text("Category already exists") }
                        } else {
                            { Text("${newCategoryName.length}/${ValidationConstants.CATEGORY_NAME_MAX_LENGTH} characters") }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newCategoryBudget,
                        onValueChange = { value -> 
                            // Apply same numeric validation as other budget fields
                            if (value.matches(ValidationConstants.AMOUNT_VALIDATION_REGEX)) {
                                newCategoryBudget = value
                            }
                        },
                        label = { Text("Budget Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                // Trigger add action if fields are valid
                                val existingCategory = uiState.allCategories.find { 
                                    it.name.trim().equals(newCategoryName.trim(), ignoreCase = true) 
                                }
                                if (existingCategory == null && newCategoryName.isNotBlank()) {
                                    viewModel.addCategory(newCategoryName, newCategoryBudget.toDoubleOrNull() ?: 0.0)
                                    showAddCategoryDialog = false
                                    newCategoryName = ""
                                    newCategoryBudget = ""
                                    showDuplicateError = false
                                }
                            }
                        ),
                        supportingText = if (uiState.hasTotalBudget) {
                            {
                                Text(
                                    text = "Remaining Budget: ${selectedCurrency.symbol}${String.format("%.2f", uiState.uncategorizedBudget)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (uiState.uncategorizedBudget >= 0)
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    else
                                        MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        } else {
                            { Text ("") }
                        },
                        leadingIcon = {
                            Text(
                                text = selectedCurrency.symbol,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val existingCategory = uiState.allCategories.find { 
                            it.name.trim().equals(newCategoryName.trim(), ignoreCase = true) 
                        }
                        
                        if (existingCategory != null) {
                            showDuplicateError = true
                        } else if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(newCategoryName, newCategoryBudget.toDoubleOrNull() ?: 0.0)
                            showAddCategoryDialog = false
                            newCategoryName = ""
                            newCategoryBudget = ""
                            showDuplicateError = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddCategoryDialog = false
                        newCategoryName = ""
                        newCategoryBudget = ""
                        showDuplicateError = false
                        if (newCategoryName.isNotBlank() || newCategoryBudget.isNotBlank()) {
                            viewModel.showCancelMessage("Category creation cancelled")
                        }
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
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