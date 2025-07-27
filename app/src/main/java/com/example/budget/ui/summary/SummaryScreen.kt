package com.example.budget.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.budget.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    navController: NavController,
    month: Int,
    year: Int,
    viewModel: SummaryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(month, year) {
        viewModel.initialize(month, year)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Summary for $month/$year") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Icon for back
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SummaryHeader()
            }
            items(uiState.summary.values.toList()) { row ->
                SummaryRow(row)
            }
        }
    }
}

@Composable
fun SummaryHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Category", fontWeight = FontWeight.Bold)
        Text("Budgeted", fontWeight = FontWeight.Bold)
        Text("Actual", fontWeight = FontWeight.Bold)
        Text("Delta", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SummaryRow(row: SummaryRow) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(row.category.name)
        Text(String.format("%.2f", row.budgeted))
        Text(String.format("%.2f", row.actual))
        Text(
            text = String.format("%.2f", row.delta),
            color = if (row.delta >= 0) Color.Green else Color.Red
        )
    }
} 