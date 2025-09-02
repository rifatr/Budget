package com.example.budget.ui.categorymanager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.budget.BudgetApp
import com.example.budget.ui.AppViewModelProvider
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
    var showSortMenu by remember { mutableStateOf(false) }
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
                },
                actions = {
                    // Sort
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.displayName) },
                                    onClick = {
                                        viewModel.updateSortOption(option)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (uiState.sortOption == option) {
                                            Icon(
                                                Icons.Default.Sort,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                            }
                        }
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
                    // Search field
                    item {
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
            }
        )
    }

    // Rename Category Dialog
    if (showRenameCategoryDialog && categoryToRename != null) {
        RenameCategoryDialog(
            currentName = categoryToRename!!.category.name,
            onDismiss = {
                showRenameCategoryDialog = false
                categoryToRename = null
            },
            onRename = { newName ->
                viewModel.renameCategory(categoryToRename!!.category.id, newName)
                showRenameCategoryDialog = false
                categoryToRename = null
            }
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
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category name and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = categoryWithStats.category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Row {
                    IconButton(
                        onClick = onRename,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${categoryWithStats.expenseCount} expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${selectedCurrency.symbol}${String.format(Locale.US, "%.2f", categoryWithStats.totalAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (categoryWithStats.totalAmount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = "total spent",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                placeholder = { Text("e.g., Food, Transport") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(categoryName) },
                enabled = categoryName.trim().isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun RenameCategoryDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf(currentName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename Category") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Category Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onRename(categoryName) },
                enabled = categoryName.trim().isNotBlank() && categoryName.trim() != currentName
            ) {
                Text("Rename")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
