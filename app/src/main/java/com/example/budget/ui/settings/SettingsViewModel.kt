package com.example.budget.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budget.data.BudgetData
import com.example.budget.data.BudgetRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SettingsUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val lastOperationStatus: String? = null,
    val lastOperationSuccess: Boolean = true
)

class SettingsViewModel(private val budgetRepository: BudgetRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .setPrettyPrinting()
        .create()

    fun exportData(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, lastOperationStatus = null)
            try {
                val budgetData = budgetRepository.getBudgetData()
                val json = gson.toJson(budgetData)
                context.contentResolver.openFileDescriptor(uri, "w")?.use {
                    FileOutputStream(it.fileDescriptor).use { stream ->
                        stream.write(json.toByteArray())
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    lastOperationStatus = "Data exported successfully!",
                    lastOperationSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    lastOperationStatus = "Export failed: ${e.message}",
                    lastOperationSuccess = false
                )
                e.printStackTrace()
            }
        }
    }

    fun importData(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, lastOperationStatus = null)
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use(BufferedReader::readText)
                if (json != null) {
                    val budgetData = gson.fromJson(json, BudgetData::class.java)
                    budgetRepository.importBudgetData(budgetData)
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        lastOperationStatus = "Data imported successfully!",
                        lastOperationSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        lastOperationStatus = "Import failed: Could not read file",
                        lastOperationSuccess = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    lastOperationStatus = "Import failed: ${e.message}",
                    lastOperationSuccess = false
                )
                e.printStackTrace()
            }
        }
    }

    fun clearStatus() {
        _uiState.value = _uiState.value.copy(lastOperationStatus = null)
    }
} 