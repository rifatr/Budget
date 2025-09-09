package com.example.budget.ui.more

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.budget.BudgetApp
import com.example.budget.data.SummaryLayoutType
import com.example.budget.ui.Screen
import com.example.budget.ui.setup.CurrencySelectionDialog
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as BudgetApp
    val selectedCurrency by app.container.currencyPreferences.selectedCurrency.collectAsState()
    val summaryLayoutType by app.container.summaryLayoutPreferences.summaryLayoutType.collectAsState()
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var isLayoutSectionExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("More") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // App Management Section
            SectionHeader("App Management")
            
            MenuItem(
                icon = Icons.Default.AttachMoney,
                title = "Currency",
                subtitle = "${selectedCurrency.symbol} ${selectedCurrency.displayName} (${selectedCurrency.code})",
                onClick = { showCurrencyDialog = true }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            AnimatedMenuItem(
                icon = Icons.Default.ViewModule,
                title = "Summary Layout",
                subtitle = if (summaryLayoutType == SummaryLayoutType.CARDS) "Cards View" else "Table View",
                onClick = { isLayoutSectionExpanded = !isLayoutSectionExpanded },
                isExpanded = isLayoutSectionExpanded
            )
            
            // Animated collapsible summary layout options
            AnimatedVisibility(
                visible = isLayoutSectionExpanded,
                enter = expandVertically(
                    animationSpec = tween(300, easing = EaseInOut)
                ) + fadeIn(
                    animationSpec = tween(300, easing = EaseInOut)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(300, easing = EaseInOut)
                ) + fadeOut(
                    animationSpec = tween(300, easing = EaseInOut)
                )
            ) {
                Column(
                    modifier = Modifier.padding(start = 56.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    // Cards Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                app.container.summaryLayoutPreferences.setSummaryLayoutType(SummaryLayoutType.CARDS)
                            }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = summaryLayoutType == SummaryLayoutType.CARDS,
                            onClick = {
                                app.container.summaryLayoutPreferences.setSummaryLayoutType(SummaryLayoutType.CARDS)
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Cards View",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Modern card-based layout",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Table Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                app.container.summaryLayoutPreferences.setSummaryLayoutType(SummaryLayoutType.TABLE)
                            }
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = summaryLayoutType == SummaryLayoutType.TABLE,
                            onClick = {
                                app.container.summaryLayoutPreferences.setSummaryLayoutType(SummaryLayoutType.TABLE)
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Table View",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Compact table with all data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            MenuItem(
                icon = Icons.Default.Category,
                title = "Categories",
                subtitle = "Add, rename, and delete categories",
                onClick = { navController.navigate(Screen.CategoryManager.route) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            MenuItem(
                icon = Icons.Default.Storage,
                title = "Data",
                subtitle = "Export & Import data",
                onClick = { navController.navigate(Screen.Settings.route) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Information Section
            SectionHeader("Information")
            
            MenuItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "App info & developer credits",
                onClick = { navController.navigate(Screen.Info.route) }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // App Info
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Budget Tracker",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manage your monthly budgets and track expenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    
    // Currency Selection Dialog
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            initialCurrency = selectedCurrency,
            onCurrencySelected = { currency ->
                app.container.currencyPreferences.setSelectedCurrency(currency)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingIcon: ImageVector = Icons.Default.ChevronRight
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AnimatedMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isExpanded: Boolean
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(300, easing = EaseInOut),
        label = "expand_icon_rotation"
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer {
                    rotationZ = rotationAngle
                }
        )
    }
}