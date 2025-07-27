package com.example.budget.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.style.TextOverflow
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SummaryHeader()
                }
                item {
                    HorizontalDivider()
                }
                items(uiState.summary.values.toList()) { row ->
                    SummaryRow(row)
                }
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
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
        Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
        Text("Budgeted", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        Text("Expense", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
        Text("Delta", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End)
    }
}

@Composable
fun SummaryRow(row: SummaryRow) {
    val positiveDeltaColor = Color(0xFF388E3C) // A more subtle green
    val negativeDeltaColor = MaterialTheme.colorScheme.error
    val progress = if (row.budgeted > 0) (row.actual / row.budgeted).toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(row.category.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(String.format("%.2f", row.budgeted), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, maxLines = 1)
                Text(String.format("%.2f", row.actual), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, maxLines = 1)
                Text(
                    text = String.format("%.2f", row.delta),
                    color = if (row.delta >= 0) positiveDeltaColor else negativeDeltaColor,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1.5f),
                    textAlign = TextAlign.End,
                    maxLines = 1
                )
            }
            if (row.budgeted > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = if (progress > 1f) negativeDeltaColor else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
fun SummaryTotals(uiState: SummaryUiState) {
    val totalBudgeted = uiState.summary.values.sumOf { it.budgeted }
    val totalActual = uiState.summary.values.sumOf { it.actual }
    val totalDelta = totalBudgeted - totalActual
    val positiveDeltaColor = Color(0xFF388E3C)
    val negativeDeltaColor = MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Totals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
        Text(String.format("%.2f", totalBudgeted), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, maxLines = 1)
        Text(String.format("%.2f", totalActual), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.End, maxLines = 1)
        Text(
            text = String.format("%.2f", totalDelta),
            color = if (totalDelta >= 0) positiveDeltaColor else negativeDeltaColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1.5f),
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
} 