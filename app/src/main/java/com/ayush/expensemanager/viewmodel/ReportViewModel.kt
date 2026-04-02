package com.ayush.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ayush.expensemanager.data.AppDatabase
import com.ayush.expensemanager.data.dao.CategoryTotal
import com.ayush.expensemanager.data.entities.Salary
import com.ayush.expensemanager.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(db.salaryDao(), db.categoryDao(), db.expenseDao(), db.emergencyFundDao())
    }

    suspend fun getReportData(month: Int, year: Int): ReportData {
        val monthStr = String.format("%02d", month)
        val yearStr = year.toString()
        val salary = repository.getSalaryForMonthSync(month, year)
        val totalSpent = repository.getTotalForMonthSync(monthStr, yearStr)
        val expenses = repository.getExpensesForMonthSync(monthStr, yearStr)
        val categoryTotals = repository.getCategoryTotalsForMonth(monthStr, yearStr)
        return ReportData(salary, totalSpent, expenses, categoryTotals)
    }
}

data class ReportData(
    val salary: Salary?,
    val totalSpent: Double,
    val expenses: List<com.ayush.expensemanager.data.entities.Expense>,
    val categoryTotals: List<CategoryTotal>
)
