package com.ayush.expensemanager.data.repository

import androidx.lifecycle.LiveData
import com.ayush.expensemanager.data.dao.CategoryDao
import com.ayush.expensemanager.data.dao.CategoryTotal
import com.ayush.expensemanager.data.dao.ExpenseDao
import com.ayush.expensemanager.data.dao.SalaryDao
import com.ayush.expensemanager.data.entities.Category
import com.ayush.expensemanager.data.entities.Expense
import com.ayush.expensemanager.data.entities.Salary

class ExpenseRepository(
    private val salaryDao: SalaryDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao
) {
    // Salary
    fun getSalaryForMonth(month: Int, year: Int): LiveData<Salary?> =
        salaryDao.getSalaryForMonth(month, year)

    suspend fun getSalaryForMonthSync(month: Int, year: Int): Salary? =
        salaryDao.getSalaryForMonthSync(month, year)

    suspend fun insertSalary(salary: Salary): Long = salaryDao.insertSalary(salary)

    fun getAllSalaries(): LiveData<List<Salary>> = salaryDao.getAllSalaries()

    suspend fun getAllSalariesSync(): List<Salary> = salaryDao.getAllSalariesSync()

    // Categories
    fun getAllCategories(): LiveData<List<Category>> = categoryDao.getAllCategories()

    suspend fun getAllCategoriesSync(): List<Category> = categoryDao.getAllCategoriesSync()

    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)

    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    suspend fun getCategoryCount(): Int = categoryDao.getCategoryCount()

    // Expenses
    fun getAllExpenses(): LiveData<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun getAllExpensesSync(): List<Expense> = expenseDao.getAllExpensesSync()

    fun getExpensesForMonth(month: String, year: String): LiveData<List<Expense>> =
        expenseDao.getExpensesForMonth(month, year)

    suspend fun getExpensesForMonthSync(month: String, year: String): List<Expense> =
        expenseDao.getExpensesForMonthSync(month, year)

    fun getTotalForMonth(month: String, year: String): LiveData<Double> =
        expenseDao.getTotalForMonth(month, year)

    suspend fun getTotalForMonthSync(month: String, year: String): Double =
        expenseDao.getTotalForMonthSync(month, year)

    suspend fun getCategoryTotalsForMonth(month: String, year: String): List<CategoryTotal> =
        expenseDao.getCategoryTotalsForMonth(month, year)

    fun getRecentExpenses(): LiveData<List<Expense>> = expenseDao.getRecentExpenses()

    fun searchExpenses(query: String): LiveData<List<Expense>> =
        expenseDao.searchExpenses(query)

    suspend fun insertExpense(expense: Expense): Long = expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: Expense) = expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) = expenseDao.deleteExpense(expense)
}
