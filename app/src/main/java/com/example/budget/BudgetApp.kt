package com.example.budget

import android.app.Application
import com.example.budget.data.AppContainer
import com.example.budget.data.AppDataContainer

class BudgetApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
} 