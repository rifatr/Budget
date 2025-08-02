package com.example.budget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.example.budget.data.Currency
import com.example.budget.ui.BudgetAppNavigation
import com.example.budget.ui.setup.CurrencySelectionDialog
import com.example.budget.ui.theme.BudgetTheme
import com.example.budget.ui.theme.GradientBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetTheme {
                GradientBackground {
                    MainContent()
                }
            }
        }
    }
}

@Composable
private fun MainContent() {
    val app = LocalContext.current.applicationContext as BudgetApp
    val isFirstLaunch by app.container.currencyPreferences.isFirstLaunch.collectAsState()
    
    if (isFirstLaunch) {
        CurrencySelectionDialog(
            onCurrencySelected = { currency ->
                app.container.currencyPreferences.setSelectedCurrency(currency)
                app.container.currencyPreferences.markFirstLaunchComplete()
            }
        )
    } else {
        BudgetAppNavigation()
    }
} 