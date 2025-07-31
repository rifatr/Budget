package com.example.budget.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.budget.ui.AppViewModelProvider
import com.example.budget.ui.Screen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Tracker") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            MonthYearSelector(
                selectedMonth = uiState.selectedMonth,
                selectedYear = uiState.selectedYear,
                onDateChange = { month, year -> viewModel.onDateChange(month, year) }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SquareButton(
                    onClick = { navController.navigate(Screen.Budget.createRoute(uiState.selectedMonth, uiState.selectedYear)) },
                    icon = Icons.Default.AccountBalanceWallet,
                    text = "Budget",
                    contentDescription = "Budget",
                    modifier = Modifier.weight(1f)
                )
                SquareButton(
                    onClick = { navController.navigate(Screen.Expense.route) },
                    icon = Icons.Default.MonetizationOn,
                    text = "Expense",
                    contentDescription = "Expense",
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SquareButton(
                    onClick = { navController.navigate(Screen.Summary.createRoute(uiState.selectedMonth, uiState.selectedYear)) },
                    icon = Icons.Default.Assessment,
                    text = "Summary",
                    contentDescription = "Summary",
                    modifier = Modifier.weight(1f)
                )
                SquareButton(
                    onClick = { navController.navigate(Screen.Settings.route) },
                    icon = Icons.Default.Settings,
                    text = "Settings",
                    contentDescription = "Settings",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MonthYearSelector(
    selectedMonth: Int,
    selectedYear: Int,
    onDateChange: (Int, Int) -> Unit
) {
    val months = (1..12).map {
        val monthDate = Calendar.getInstance().apply { set(Calendar.MONTH, it - 1) }.time
        SimpleDateFormat("MMMM", Locale.getDefault()).format(monthDate)
    }
    val years = (selectedYear - 5..selectedYear + 5).toList()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Dropdown(
            items = months,
            selectedIndex = selectedMonth - 1,
            onItemSelected = { index -> onDateChange(index + 1, selectedYear) }
        )
        Dropdown(
            items = years.map { it.toString() },
            selectedIndex = years.indexOf(selectedYear),
            onItemSelected = { index -> onDateChange(selectedMonth, years[index]) }
        )
    }
}

@Composable
fun Dropdown(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(items[selectedIndex])
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(index)
                        expanded = false
                    }
                )
            }
        }
    }
} 

@Composable
fun SquareButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
} 