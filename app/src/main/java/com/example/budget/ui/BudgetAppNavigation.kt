package com.example.budget.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.budget.ui.budget.BudgetScreen
import com.example.budget.ui.categorymanager.CategoryManagerScreen
import com.example.budget.ui.expense.ExpenseScreen
import com.example.budget.ui.expensehistory.ExpenseHistoryScreen
import com.example.budget.ui.info.InfoScreen
import com.example.budget.ui.more.MoreScreen
import com.example.budget.ui.settings.SettingsScreen
import com.example.budget.ui.summary.SummaryScreen
import kotlinx.coroutines.launch

data class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    val label: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BudgetAppNavigation(
    navController: NavHostController = rememberNavController(),
    startWithExpenseScreen: Boolean = false
) {
    val bottomNavItems = listOf(
        BottomNavItem(Screen.Expense, Icons.Default.MonetizationOn, "Expense"),
        BottomNavItem(Screen.Budget, Icons.Default.AccountBalanceWallet, "Budget"),
        BottomNavItem(Screen.Summary, Icons.Default.Assessment, "Summary"),
        BottomNavItem(Screen.More, Icons.Default.MoreHoriz, "More")
    )

    // Pager state for swipe navigation
    val pagerState = rememberPagerState(
        initialPage = if (startWithExpenseScreen) 0 else 0,
        pageCount = { bottomNavItems.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // Get current navigation state
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    
    // Sync pager with navigation
    LaunchedEffect(startWithExpenseScreen) {
        if (startWithExpenseScreen) {
            pagerState.scrollToPage(0) // Expense tab
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                item.icon, 
                                contentDescription = item.label,
                                tint = if (pagerState.currentPage == index) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        },
                        label = { 
                            Text(
                                item.label,
                                color = if (pagerState.currentPage == index) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            ) 
                        },
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                // If we're on a secondary screen (not hidden), pop back to main navigation
                                if (currentRoute != null && currentRoute != "hidden") {
                                    navController.popBackStack("hidden", inclusive = false)
                                }
                                // Then navigate to the selected tab
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            SwipeableTabPager(
                pagerState = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> ExpenseScreen(navController)
                    1 -> BudgetScreen()
                    2 -> SummaryScreen(navController)
                    3 -> MoreScreen(navController)
                }
            }
            
            // Handle navigation to non-main tabs (Info, Settings)
            NavHost(
                navController = navController,
                startDestination = "hidden", // Hidden route for non-main screens
                modifier = Modifier.fillMaxSize()
            ) {
                composable("hidden") { 
                    // Empty composable for the hidden start destination
                }
                composable(Screen.Info.route) {
                    InfoScreen(navController)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(navController)
                }
                composable(Screen.CategoryManager.route) {
                    CategoryManagerScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable("${Screen.CategoryExpenseDetail.route}/{categoryId}/{categoryName}/{month}/{year}") { backStackEntry ->
                    val categoryId = backStackEntry.arguments?.getString("categoryId")?.toIntOrNull() ?: 0
                    val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                    val month = backStackEntry.arguments?.getString("month")?.toIntOrNull() ?: 1
                    val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() ?: 2024
                    
                    com.example.budget.ui.categoryexpensedetail.CategoryExpenseDetailScreen(
                        categoryId = categoryId,
                        categoryName = categoryName,
                        month = month,
                        year = year,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.ExpenseHistory.route) {
                    ExpenseHistoryScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SwipeableTabPager(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    content: @Composable (page: Int) -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }
    var dragStartX by remember { mutableStateOf(0f) }
    var currentDragAmount by remember { mutableStateOf(0f) }
    
    HorizontalPager(
        state = pagerState,
        modifier = modifier.pointerInput(pagerState.currentPage) {
            detectHorizontalDragGestures(
                onDragStart = { offset ->
                    isDragging = true
                    dragStartX = offset.x
                    currentDragAmount = 0f
                },
                onDragEnd = {
                    isDragging = false
                    currentDragAmount = 0f
                },
                onHorizontalDrag = { change, dragAmount ->
                    currentDragAmount += dragAmount
                    val currentPage = pagerState.currentPage
                    
                    // Calculate intended swipe direction
                    val swipeDirection = if (currentDragAmount > 0) "left" else "right"
                    
                    // Block left swipe on Expense tab (can't go to previous page)
                    if (currentPage == 0 && swipeDirection == "left") {
                        change.consume()
                        return@detectHorizontalDragGestures
                    }
                    
                    // Block right swipe on More tab (can't go to next page)  
                    if (currentPage == 3 && swipeDirection == "right") {
                        change.consume()
                        return@detectHorizontalDragGestures
                    }
                }
            )
        },
        userScrollEnabled = true
    ) { page ->
        content(page)
    }
} 