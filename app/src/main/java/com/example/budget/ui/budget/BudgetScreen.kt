package com.example.budget.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.budget.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navController: NavController,
    month: Int,
    year: Int,
    viewModel: BudgetViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var showDuplicateError by remember { mutableStateOf(false) }

    LaunchedEffect(month, year) {
        viewModel.initialize(month, year)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget for $month/$year") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.saveBudget()
                navController.popBackStack()
            }) {
                Icon(Icons.Default.Done, contentDescription = "Save Budget")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // Add bottom padding for FAB
        ) {
            item {
                OutlinedTextField(
                    value = uiState.overallBudgetInput,
                    onValueChange = { viewModel.onOverallBudgetChange(it) },
                    label = { Text("Overall Budget") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Category Budgets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Button(onClick = { showAddCategoryDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Category")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Category")
                    }
                }
            }

            items(uiState.allCategories) { category ->
                OutlinedTextField(
                    value = uiState.categoryBudgetsInput[category.id] ?: "",
                    onValueChange = { viewModel.onCategoryBudgetChange(category.id, it) },
                    label = { Text(category.name) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { 
                showAddCategoryDialog = false
                newCategoryName = ""
                showDuplicateError = false
            },
            title = { Text("Add New Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { 
                            newCategoryName = it
                            showDuplicateError = false // Reset error when user types
                        },
                        label = { Text("Category Name") },
                        isError = showDuplicateError,
                        supportingText = if (showDuplicateError) {
                            { Text("Category already exists", color = MaterialTheme.colorScheme.error) }
                        } else null
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = newCategoryName.trim()
                        if (trimmedName.isNotBlank()) {
                            // Check for duplicates in UI state
                            val categoryExists = uiState.allCategories.any { 
                                it.name.equals(trimmedName, ignoreCase = true) 
                            }
                            
                            if (categoryExists) {
                                showDuplicateError = true
                            } else {
                                viewModel.addCategory(trimmedName)
                                newCategoryName = ""
                                showAddCategoryDialog = false
                                showDuplicateError = false
                            }
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(onClick = { 
                    showAddCategoryDialog = false
                    newCategoryName = ""
                    showDuplicateError = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
} 