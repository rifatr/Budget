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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(newCategoryName)
                            newCategoryName = ""
                            showAddCategoryDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 