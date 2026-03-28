package com.ayush.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.ayush.expensemanager.data.AppDatabase
import com.ayush.expensemanager.data.entities.Expense
import com.ayush.expensemanager.data.entities.Salary
import com.ayush.expensemanager.data.repository.ExpenseRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val currentCalendar = Calendar.getInstance()

    private val _currentMonth = MutableLiveData<Int>(currentCalendar.get(Calendar.MONTH) + 1)
    private val _currentYear = MutableLiveData<Int>(currentCalendar.get(Calendar.YEAR))

    val currentMonth: LiveData<Int> = _currentMonth
    val currentYear: LiveData<Int> = _currentYear

    val allCategories: LiveData<List<com.ayush.expensemanager.data.entities.Category>>
    val recentExpenses: LiveData<List<Expense>>

    private val _currentSalary = MediatorLiveData<Salary?>()
    val currentSalary: LiveData<Salary?> = _currentSalary

    private val _totalSpent = MediatorLiveData<Double>()
    val totalSpent: LiveData<Double> = _totalSpent

    private val _monthlyExpenses = MediatorLiveData<List<Expense>>()
    val monthlyExpenses: LiveData<List<Expense>> = _monthlyExpenses

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(db.salaryDao(), db.categoryDao(), db.expenseDao())
        allCategories = repository.getAllCategories()
        recentExpenses = repository.getRecentExpenses()

        observeMonthData()
    }

    private var salarySource: LiveData<Salary?>? = null
    private var totalSpentSource: LiveData<Double>? = null
    private var monthlyExpensesSource: LiveData<List<Expense>>? = null

    private fun observeMonthData() {
        val month = _currentMonth.value ?: return
        val year = _currentYear.value ?: return
        val monthStr = String.format("%02d", month)
        val yearStr = year.toString()

        // Remove old sources
        salarySource?.let { _currentSalary.removeSource(it) }
        totalSpentSource?.let { _totalSpent.removeSource(it) }
        monthlyExpensesSource?.let { _monthlyExpenses.removeSource(it) }

        // Add new sources
        salarySource = repository.getSalaryForMonth(month, year).also {
            _currentSalary.addSource(it) { salary -> _currentSalary.value = salary }
        }
        totalSpentSource = repository.getTotalForMonth(monthStr, yearStr).also {
            _totalSpent.addSource(it) { total -> _totalSpent.value = total }
        }
        monthlyExpensesSource = repository.getExpensesForMonth(monthStr, yearStr).also {
            _monthlyExpenses.addSource(it) { expenses -> _monthlyExpenses.value = expenses }
        }
    }

    fun setMonth(month: Int, year: Int) {
        _currentMonth.value = month
        _currentYear.value = year
        observeMonthData()
    }

    fun saveSalary(amount: Double, month: Int, year: Int) = viewModelScope.launch {
        val salary = Salary(amount = amount, month = month, year = year)
        repository.insertSalary(salary)
    }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        repository.deleteExpense(expense)
    }

    suspend fun getCategoryTotals(month: Int, year: Int): List<com.ayush.expensemanager.data.dao.CategoryTotal> {
        val monthStr = String.format("%02d", month)
        val yearStr = year.toString()
        return repository.getCategoryTotalsForMonth(monthStr, yearStr)
    }
}
