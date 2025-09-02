package com.example.budget.ui.categorymanager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budget.BudgetApp
import com.example.budget.ui.AppViewModelProvider
import com.example.budget.ui.budget.ValidationConstants
import com.example.budget.ui.components.ConfirmationMessage
import androidx.compose.runtime.collectAsState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoryManagerViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val app = context.applicationContext as BudgetApp
    val selectedCurrency by app.container.currencyPreferences.selectedCurrency.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showRenameCategoryDialog by remember { mutableStateOf(false) }
    var categoryToRename by remember { mutableStateOf<CategoryWithStats?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryWithStats?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.filteredAndSortedCategories.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (uiState.searchQuery.isBlank()) {
                            "No categories yet"
                        } else {
                            "No categories found for \"${uiState.searchQuery}\""
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.searchQuery.isBlank()) {
                            "Tap the + button to add your first category"
                        } else {
                            "Try adjusting your search query"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Search and Sort Controls
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Search field
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                label = { Text("Search categories") },
                                placeholder = { Text("Type to search...") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (uiState.searchQuery.isNotBlank()) {
                                        IconButton(
                                            onClick = { viewModel.updateSearchQuery("") }
                                        ) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            
                            // Sort buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SortOption.values().forEach { option ->
                                    SortButton(
                                        sortOption = option,
                                        isSelected = uiState.sortOption == option,
                                        isAscending = uiState.sortAscending,
                                        onClick = { viewModel.toggleSort(option) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                    
                    items(uiState.filteredAndSortedCategories) { categoryWithStats ->
                        CategoryCard(
                            categoryWithStats = categoryWithStats,
                            selectedCurrency = selectedCurrency,
                            onRename = {
                                categoryToRename = categoryWithStats
                                showRenameCategoryDialog = true
                            },
                            onDelete = {
                                categoryToDelete = categoryWithStats
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAdd = { name ->
                viewModel.addCategory(name)
                showAddCategoryDialog = false
            },
            viewModel = viewModel
        )
    }

    // Rename Category Dialog
    if (showRenameCategoryDialog && categoryToRename != null) {
        RenameCategoryDialog(
            currentName = categoryToRename!!.category.name,
            categoryId = categoryToRename!!.category.id,
            onDismiss = {
                showRenameCategoryDialog = false
                categoryToRename = null
            },
            onRename = { newName ->
                viewModel.renameCategory(categoryToRename!!.category.id, newName)
                showRenameCategoryDialog = false
                categoryToRename = null
            },
            viewModel = viewModel
        )
    }

    // Delete Category Confirmation
    categoryToDelete?.let { categoryWithStats ->
        DeleteCategoryDialog(
            categoryWithStats = categoryWithStats,
            selectedCurrency = selectedCurrency,
            onDismiss = { categoryToDelete = null },
            onConfirm = {
                viewModel.deleteCategory(categoryWithStats)
                categoryToDelete = null
            }
        )
    }
}

@Composable
fun CategoryCard(
    categoryWithStats: CategoryWithStats,
    selectedCurrency: com.example.budget.data.Currency,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category info section
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = categoryWithStats.category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Expense count
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${categoryWithStats.expenseCount}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "expenses",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Total amount
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${selectedCurrency.symbol}${String.format(Locale.US, "%.2f", categoryWithStats.totalAmount)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (categoryWithStats.totalAmount > 0) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = "total spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onRename,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Rename",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
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
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit,
    viewModel: CategoryManagerViewModel
) {
    var categoryName by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    
    // Clear validation errors when dialog opens
    LaunchedEffect(Unit) {
        viewModel.clearValidationErrors()
    }
    
    AlertDialog(
        onDismissRequest = {
            viewModel.clearValidationErrors()
            onDismiss()
        },
        title = { Text("Add Category") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { newValue ->
                    // Limit category name to max length
                    if (newValue.length <= ValidationConstants.CATEGORY_NAME_MAX_LENGTH) {
                        categoryName = newValue
                        // Validate in real-time
                        viewModel.validateCategoryName(newValue)
                    }
                },
                label = { Text("Category Name") },
                placeholder = { Text("e.g., Food, Transport") },
                isError = uiState.showDuplicateError,
                supportingText = {
                    if (uiState.showDuplicateError) {
                        Text(
                            text = uiState.duplicateErrorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("${categoryName.length}/${ValidationConstants.CATEGORY_NAME_MAX_LENGTH} characters")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (categoryName.trim().isNotBlank() && !uiState.showDuplicateError) {
                            onAdd(categoryName)
                            viewModel.clearValidationErrors()
                        }
                    }
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    onAdd(categoryName)
                    viewModel.clearValidationErrors()
                },
                enabled = categoryName.trim().isNotBlank() && !uiState.showDuplicateError
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.clearValidationErrors()
                onDismiss()
            }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameCategoryDialog(
    currentName: String,
    categoryId: Int,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    viewModel: CategoryManagerViewModel
) {
    var categoryName by remember { mutableStateOf(currentName) }
    val uiState by viewModel.uiState.collectAsState()
    
    // Clear validation errors when dialog opens
    LaunchedEffect(Unit) {
        viewModel.clearValidationErrors()
    }
    
    AlertDialog(
        onDismissRequest = {
            viewModel.clearValidationErrors()
            onDismiss()
        },
        title = { Text("Rename Category") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { newValue ->
                    // Limit category name to max length
                    if (newValue.length <= ValidationConstants.CATEGORY_NAME_MAX_LENGTH) {
                        categoryName = newValue
                        // Validate in real-time, excluding current category
                        viewModel.validateCategoryName(newValue, excludeCategoryId = categoryId)
                    }
                },
                label = { Text("Category Name") },
                isError = uiState.showDuplicateError,
                supportingText = {
                    if (uiState.showDuplicateError) {
                        Text(
                            text = uiState.duplicateErrorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text("${categoryName.length}/${ValidationConstants.CATEGORY_NAME_MAX_LENGTH} characters")
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (categoryName.trim().isNotBlank() && 
                            categoryName.trim() != currentName && 
                            !uiState.showDuplicateError) {
                            onRename(categoryName)
                            viewModel.clearValidationErrors()
                        }
                    }
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    onRename(categoryName)
                    viewModel.clearValidationErrors()
                },
                enabled = categoryName.trim().isNotBlank() && 
                         categoryName.trim() != currentName && 
                         !uiState.showDuplicateError
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.clearValidationErrors()
                onDismiss()
            }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteCategoryDialog(
    categoryWithStats: CategoryWithStats,
    selectedCurrency: com.example.budget.data.Currency,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val category = categoryWithStats.category
    val expenseCount = categoryWithStats.expenseCount
    val totalAmount = categoryWithStats.totalAmount
    
    var showSecondConfirmation by remember { mutableStateOf(false) }
    
    if (!showSecondConfirmation) {
        // First confirmation dialog
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Delete Category") },
            text = {
                Column {
                    if (expenseCount == 0) {
                        Text("Are you sure you want to delete '${category.name}'?")
                    } else {
                        Text(
                            "Category '${category.name}' has $expenseCount expenses totaling " +
                            "${selectedCurrency.symbol}${String.format(Locale.US, "%.2f", totalAmount)}.\n\n" +
                            "Deleting this category will also delete ALL its expenses. Do you want to continue?"
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (expenseCount == 0) {
                            onConfirm()
                        } else {
                            showSecondConfirmation = true
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    } else {
        // Second confirmation dialog (only for categories with expenses)
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { 
                Text(
                    "Are you absolutely sure?",
                    color = MaterialTheme.colorScheme.error
                ) 
            },
            text = {
                Text(
                    "This will permanently delete the category '${category.name}' and ALL its $expenseCount expenses. " +
                    "This action cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Yes, Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SortButton(
    sortOption: SortOption,
    isSelected: Boolean,
    isAscending: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = when (sortOption) {
                    SortOption.BY_NAME -> "Name"
                    SortOption.BY_USAGE -> "Usage"
                    SortOption.BY_AMOUNT -> "Amount"
                },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
            if (isSelected) {
                Icon(
                    imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = if (isAscending) "Ascending" else "Descending",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
