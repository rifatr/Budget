package com.example.budget.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    LaunchedEffect(month, year) {
        viewModel.initialize(month, year)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget for $month/$year") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Icon for back
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.saveBudget()
                navController.popBackStack()
            }) {
                // Icon for save
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
} 