package com.ayush.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.ayush.expensemanager.data.AppDatabase
import com.ayush.expensemanager.data.entities.Category
import com.ayush.expensemanager.data.entities.Expense
import com.ayush.expensemanager.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    val allCategories: LiveData<List<Category>>
    val allExpenses: LiveData<List<Expense>>

    private val _searchQuery = MutableLiveData<String>("")
    val searchResults: LiveData<List<Expense>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(db.salaryDao(), db.categoryDao(), db.expenseDao(), db.emergencyFundDao())
        allCategories = repository.getAllCategories()
        allExpenses = repository.getAllExpenses()
        searchResults = _searchQuery.switchMap { query ->
            if (query.isBlank()) repository.getAllExpenses()
            else repository.searchExpenses(query)
        }
    }

    fun addExpense(expense: Expense) = viewModelScope.launch {
        repository.insertExpense(expense)
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repository.updateExpense(expense)
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    fun searchExpenses(query: String) {
        _searchQuery.value = query
    }

    fun getExpensesForMonth(month: String, year: String): LiveData<List<Expense>> =
        repository.getExpensesForMonth(month, year)
}
