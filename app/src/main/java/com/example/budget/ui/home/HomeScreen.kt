package com.example.budget.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.unit.DpOffset

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
                    IconButton(onClick = { navController.navigate(Screen.Info.route) }) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month/Year selector at the top
            MonthYearSelector(
                selectedMonth = uiState.selectedMonth,
                selectedYear = uiState.selectedYear,
                onDateChange = { month, year -> viewModel.onDateChange(month, year) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Centered 2x2 Grid of main buttons
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        SquareButton(
                            onClick = { navController.navigate(Screen.Budget.createRoute(uiState.selectedMonth, uiState.selectedYear)) },
                            icon = Icons.Default.AccountBalanceWallet,
                            text = "Budget",
                            contentDescription = "Budget",
                            modifier = Modifier.size(120.dp)
                        )
                        SquareButton(
                            onClick = { navController.navigate(Screen.Expense.route) },
                            icon = Icons.Default.MonetizationOn,
                            text = "Expense",
                            contentDescription = "Expense",
                            modifier = Modifier.size(120.dp)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        SquareButton(
                            onClick = { navController.navigate(Screen.Summary.createRoute(uiState.selectedMonth, uiState.selectedYear)) },
                            icon = Icons.Default.Assessment,
                            text = "Summary",
                            contentDescription = "Summary",
                            modifier = Modifier.size(120.dp)
                        )
                        SquareButton(
                            onClick = { navController.navigate(Screen.Settings.route) },
                            icon = Icons.Default.Settings,
                            text = "Settings",
                            contentDescription = "Settings",
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }
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
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 5 .. currentYear + 5).toList()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Dropdown(
            items = months,
            selectedIndex = selectedMonth - 1,
            onItemSelected = { index -> onDateChange(index + 1, selectedYear) }
        )
        Spacer(modifier = Modifier.width(16.dp))
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
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val maxDropdownHeight = (screenHeight * 0.4f) // 40% of screen height

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .height(48.dp)
                .widthIn(min = 100.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = items[selectedIndex],
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(0.dp, 4.dp),
            modifier = Modifier
                .heightIn(max = maxDropdownHeight)
                .widthIn(min = 140.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { 
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (index == selectedIndex) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    onClick = {
                        onItemSelected(index)
                        expanded = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
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
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
} 