package com.ayush.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.ayush.expensemanager.data.AppDatabase
import com.ayush.expensemanager.data.entities.EmergencyFundTransaction
import com.ayush.expensemanager.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class EmergencyFundViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    val allTransactions: LiveData<List<EmergencyFundTransaction>>
    val totalBalance: LiveData<Double>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(db.salaryDao(), db.categoryDao(), db.expenseDao(), db.emergencyFundDao())
        allTransactions = repository.getAllEmergencyFundTransactions()
        totalBalance = repository.getEmergencyFundBalance()
    }

    fun addTransaction(transaction: EmergencyFundTransaction) = viewModelScope.launch {
        repository.insertEmergencyFundTransaction(transaction)
    }

    fun deleteTransaction(transaction: EmergencyFundTransaction) = viewModelScope.launch {
        repository.deleteEmergencyFundTransaction(transaction)
    }
}
