package com.example.budget.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.summary.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No data for this month.", style = MaterialTheme.typography.titleMedium)
            }
        } else {
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
                item {
                    Divider()
                }
                items(uiState.summary.values.toList()) { row ->
                    SummaryRow(row)
                }
                item {
                    Divider()
                }
                item {
                    SummaryTotals(uiState)
                }
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
        Text("Category", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text("Budgeted", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Text("Actual", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Text("Delta", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
fun SummaryRow(row: SummaryRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(row.category.name, modifier = Modifier.weight(1f))
            Text(String.format("%.2f", row.budgeted), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text(String.format("%.2f", row.actual), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
            Text(
                text = String.format("%.2f", row.delta),
                color = if (row.delta >= 0) Color.Green else Color.Red,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun SummaryTotals(uiState: SummaryUiState) {
    val totalBudgeted = uiState.summary.values.sumOf { it.budgeted }
    val totalActual = uiState.summary.values.sumOf { it.actual }
    val totalDelta = totalBudgeted - totalActual

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Totals", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(String.format("%.2f", totalBudgeted), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Text(String.format("%.2f", totalActual), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Text(
            text = String.format("%.2f", totalDelta),
            color = if (totalDelta >= 0) Color.Green else Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
} 