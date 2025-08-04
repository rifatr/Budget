package com.example.budget.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import com.example.budget.BudgetApp
import com.example.budget.R
import com.example.budget.data.db.Expense
import com.example.budget.ui.budget.ValidationConstants
import java.util.*

class ExpenseWidgetConfigActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var amountInput: EditText
    private lateinit var categoryButton: Button
    private lateinit var addButton: Button
    private var categories = emptyList<com.example.budget.data.db.Category>()
    private var selectedCategory: com.example.budget.data.db.Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set result to CANCELED initially
        setResult(RESULT_CANCELED)
        
        // Get widget ID from intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContentView(R.layout.widget_config_layout)
        setupViews()
        loadCategories()
    }

    private fun setupViews() {
        amountInput = findViewById(R.id.config_amount_input)
        categoryButton = findViewById(R.id.config_category_button)
        addButton = findViewById(R.id.config_add_button)
        val cancelButton = findViewById<Button>(R.id.config_cancel_button)

        // Set up amount input validation
        setupAmountValidation()

        // Category button click
        categoryButton.setOnClickListener {
            // Hide keyboard first
            hideKeyboard()
            showCategoryDialog()
        }

        addButton.setOnClickListener {
            hideKeyboard()
            addExpense()
        }
        
        cancelButton.setOnClickListener {
            // Open the main app with proper flags to avoid background issues
            val appIntent = Intent(this, com.example.budget.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(appIntent)
            finish()
        }
        
        // Auto-focus amount input and show keyboard
        amountInput.requestFocus()
        showKeyboard()
    }

    private fun hideKeyboard() {
        try {
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun showKeyboard() {
        try {
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(amountInput, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun showCategoryDialog() {
        if (categories.isEmpty()) {
            showError("No categories available")
            return
        }

        val categoryNames = categories.map { it.name }.toTypedArray()
        val selectedIndex = categories.indexOf(selectedCategory)

        android.app.AlertDialog.Builder(this)
            .setTitle("Select Category")
            .setSingleChoiceItems(categoryNames, selectedIndex) { dialog, which ->
                selectedCategory = categories[which]
                updateCategoryButton()
                updateButtonState()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateCategoryButton() {
        categoryButton.text = selectedCategory?.name ?: "Select Category"
    }

    private fun setupAmountValidation() {
        amountInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val text = s.toString()
                
                // Only validate if text is not empty and contains invalid characters
                if (text.isNotEmpty()) {
                    // Allow digits and one decimal point
                    val validPattern = Regex("^\\d*\\.?\\d*$")
                    if (!text.matches(validPattern)) {
                        val newText = text.dropLast(1)
                        amountInput.setText(newText)
                        amountInput.setSelection(newText.length)
                        showError("Only numbers and decimal point allowed")
                    }
                    
                    // Check for reasonable limits (max 6 digits before decimal, 2 after)
                    val parts = text.split(".")
                    if (parts.size == 2 && parts[1].length > 2) {
                        // Too many decimal places
                        val newText = parts[0] + "." + parts[1].take(2)
                        amountInput.setText(newText)
                        amountInput.setSelection(newText.length)
                    } else if (parts[0].length > 6) {
                        // Too many digits before decimal
                        val newText = parts[0].take(6) + if (parts.size == 2) ".${parts[1]}" else ""
                        amountInput.setText(newText)
                        amountInput.setSelection(newText.length)
                    }
                }
                
                updateButtonState()
            }
        })
    }

    private fun updateButtonState() {
        val amount = amountInput.text.toString().trim()
        val hasValidAmount = amount.isNotEmpty() && amount.toDoubleOrNull() != null && amount.toDouble() > 0
        val hasCategory = selectedCategory != null
        
        addButton.isEnabled = hasValidAmount && hasCategory
        addButton.alpha = if (addButton.isEnabled) 1.0f else 0.5f
    }

    private fun showError(message: String) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Ignore toast errors
        }
    }

    private fun loadCategories() {
        val app = application as BudgetApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                categories = app.container.budgetRepository.getAllCategoriesByUsage().first()

                withContext(Dispatchers.Main) {
                    if (categories.isEmpty()) {
                        Toast.makeText(this@ExpenseWidgetConfigActivity, "No categories found. Please create categories in the app first.", Toast.LENGTH_LONG).show()
                        finish()
                        return@withContext
                    }

                    // Select most used category (first in the list)
                    selectedCategory = categories.firstOrNull()
                    updateCategoryButton()
                    updateButtonState()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ExpenseWidgetConfigActivity, "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun addExpense() {
        val amountStr = amountInput.text.toString().trim()
        
        // Validate amount
        if (amountStr.isEmpty()) {
            showError("Please enter an amount")
            amountInput.requestFocus()
            showKeyboard()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            showError("Please enter a valid amount greater than 0")
            amountInput.requestFocus()
            showKeyboard()
            return
        }

        if (amount > 999999.99) {
            showError("Amount too large (max: 999,999.99)")
            amountInput.requestFocus()
            showKeyboard()
            return
        }

        // Validate category
        if (selectedCategory == null) {
            showError("Please select a category")
            return
        }

        // Disable button during processing
        addButton.isEnabled = false
        addButton.text = "Adding..."

        val app = application as BudgetApp
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentDate = Date()
                
                val expense = Expense(
                    amount = amount,
                    categoryId = selectedCategory!!.id,
                    date = currentDate,
                    description = ""
                )

                app.container.budgetRepository.insertExpense(expense)
                app.container.budgetRepository.incrementCategoryUsage(selectedCategory!!.id)

                withContext(Dispatchers.Main) {
                    // Get current currency symbol
                    val currentCurrency = app.container.currencyPreferences.selectedCurrency.value
                    val currencySymbol = currentCurrency.symbol
                    
                    // Show toast and close widget immediately - DO NOT open app
                    Toast.makeText(
                        this@ExpenseWidgetConfigActivity,
                        "✅ Expense added: ${currencySymbol}${String.format("%.2f", amount)}",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Force close without bringing background app to foreground
                    moveTaskToBack(true)
                    finish()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Failed to add expense: ${e.message}")
                    addButton.isEnabled = true
                    addButton.text = "Add Expense"
                }
            }
        }
    }
} 