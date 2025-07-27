package com.example.budget.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetData
import com.example.budget.data.BudgetRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileOutputStream

class SettingsViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            val budgetData = budgetRepository.getBudgetData()
            val json = Gson().toJson(budgetData)
            try {
                context.contentResolver.openFileDescriptor(uri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { stream ->
                        stream.write(json.toByteArray())
                    }
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use(BufferedReader::readText)
                if (json != null) {
                    val budgetData = Gson().fromJson(json, BudgetData::class.java)
                    budgetRepository.importBudgetData(budgetData)
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }
} 