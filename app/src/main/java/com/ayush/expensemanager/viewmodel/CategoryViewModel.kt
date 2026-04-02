package com.ayush.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.ayush.expensemanager.data.AppDatabase
import com.ayush.expensemanager.data.entities.Category
import com.ayush.expensemanager.data.repository.ExpenseRepository
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    val allCategories: LiveData<List<Category>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(db.salaryDao(), db.categoryDao(), db.expenseDao(), db.emergencyFundDao())
        allCategories = repository.getAllCategories()
    }

    fun addCategory(name: String, colorHex: String = "#6750A4") = viewModelScope.launch {
        val category = Category(name = name, colorHex = colorHex)
        repository.insertCategory(category)
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }
}
