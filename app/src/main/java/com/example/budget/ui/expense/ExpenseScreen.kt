package com.example.budget.ui.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budget.data.db.Category
import com.example.budget.data.db.Expense
import com.example.budget.data.ValidationConstants
import com.example.budget.data.DateConstants
import com.example.budget.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.example.budget.ui.components.ConfirmationMessage
import com.example.budget.ui.utils.formatCurrency
import com.example.budget.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val app = context.applicationContext as com.example.budget.BudgetApp
    val selectedCurrency by app.container.currencyPreferences.selectedCurrency.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Keyboard and focus management
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense") }
            )
        },
        snackbarHost = {
            if (uiState.showConfirmationMessage) {
                ConfirmationMessage(
                    message = uiState.confirmationMessage,
                    isError = false,
                    onDismiss = { viewModel.dismissConfirmationMessage() }
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    })
                },
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                // Date and Category side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date selector on the left
                    Box(modifier = Modifier.weight(1f)) {
                        DateSelector(
                            date = uiState.date,
                            onDateChange = { viewModel.onDateChange(it) }
                        )
                    }
                    
                    // Category selector on the right
                    Box(modifier = Modifier.weight(1f)) {
                        CategorySelector(
                            categories = uiState.allCategories,
                            selectedCategory = uiState.category,
                            onCategoryChange = { viewModel.onCategoryChange(it) }
                        )
                    }
                }
            }
            
            item {
                // Amount field
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.onAmountChange(it) },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.amount.toDoubleOrNull() == null && uiState.amount.isNotBlank(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    leadingIcon = {
                        Text(
                            text = selectedCurrency.symbol,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
            
            item {
                // Description field
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.onDescriptionChange(it) },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    ),
                    supportingText = {
                        Text("${uiState.description.length}/${ValidationConstants.EXPENSE_DESCRIPTION_MAX_LENGTH} characters")
                    }
                )
            }
            
            item {
                // Create button
                Button(
                    onClick = { 
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.saveExpense()
                    },
                    enabled = uiState.isEntryValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = "Add Expense",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }


            if (uiState.latestExpenses.isNotEmpty()) {
                item {
                      Text(
                          text = "Latest Expenses of ${DateConstants.getMonthYearString(uiState.date)}",
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.SemiBold,
                          color = MaterialTheme.colorScheme.onSurface,
                          modifier = Modifier.padding(top = 10.dp)
                      )
                }
                
                items(uiState.latestExpenses.take(ValidationConstants.LATEST_EXPENSES_COUNT)) { expense ->
                    LatestExpenseItem(
                        expense = expense,
                        categoryMap = uiState.categoryMap,
                        currencySymbol = selectedCurrency.symbol
                    )
                }
             }
             else {
                 item {
                       Text(
                           text = "No expenses in ${DateConstants.getMonthYearString(uiState.date)}",
                           style = MaterialTheme.typography.titleMedium,
                           fontWeight = FontWeight.SemiBold,
                           color = MaterialTheme.colorScheme.onSurface,
                           modifier = Modifier.padding(top = 10.dp)
                       )
                 }
             }
            
            item {
                val calendar = Calendar.getInstance()
                calendar.time = uiState.date
                val currentMonth = calendar.get(Calendar.MONTH) + 1
                val currentYear = calendar.get(Calendar.YEAR)
                
                OutlinedButton(
                    onClick = { 
                        navController.navigate(Screen.ExpenseHistory.createRoute(currentMonth, currentYear))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "View Full History",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
fun DateSelector(date: Date, onDateChange: (Date) -> Unit) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    val calendar = Calendar.getInstance()
    calendar.time = date

    fun showDatePickerDialog() {
        val currentCalendar = Calendar.getInstance()
        currentCalendar.time = date
        
        val dialog = android.app.DatePickerDialog(
            context,
            null, // We'll set up our own listener
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )
        
        // Remove the OK and Cancel buttons and set up instant selection
        dialog.setOnShowListener {
            dialog.getButton(android.app.DatePickerDialog.BUTTON_POSITIVE)?.visibility = android.view.View.GONE
            dialog.getButton(android.app.DatePickerDialog.BUTTON_NEGATIVE)?.visibility = android.view.View.GONE
            
            // Get the DatePicker widget and set up instant selection
            val datePicker = dialog.datePicker
            datePicker.setOnDateChangedListener { _, year, month, dayOfMonth ->
                val newDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                onDateChange(newDate)
                dialog.dismiss() // Auto-dismiss after selection
            }
        }
        
        dialog.show()
    }

    Button(
        onClick = { showDatePickerDialog() },
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Date",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Change date",
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = dateFormat.format(date),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategoryChange: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val maxDropdownHeight = (screenHeight * 0.4f) // 40% of screen height

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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCategory?.name ?: "Create in Budget menu",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    if (selectedCategory != null) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, 4.dp),
            modifier = Modifier
                .heightIn(max = maxDropdownHeight)
                .widthIn(min = 140.dp, max = 200.dp)
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
fun LatestExpenseItem(
    expense: Expense,
    categoryMap: Map<Int, String>,
    currencySymbol: String
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
                .padding(12.dp),
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
                        maxLines = 1,
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
        }
    }
}
