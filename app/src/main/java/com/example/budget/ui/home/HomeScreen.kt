package com.example.budget.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                title = { Text("Budget Tracker") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MonthYearSelector(
                selectedMonth = uiState.selectedMonth,
                selectedYear = uiState.selectedYear,
                onDateChange = { month, year -> viewModel.onDateChange(month, year) }
            )
            Button(
                onClick = { navController.navigate(Screen.Budget.createRoute(uiState.selectedMonth, uiState.selectedYear)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Budget")
            }
            Button(
                onClick = { navController.navigate(Screen.Expense.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Expense")
            }
            Button(
                onClick = { navController.navigate(Screen.Summary.createRoute(uiState.selectedMonth, uiState.selectedYear)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Summary")
            }
            Button(
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Settings")
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