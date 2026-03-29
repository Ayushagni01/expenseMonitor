package com.ayush.expensemanager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ayush.expensemanager.data.entities.Expense

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAllExpenses(): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    suspend fun getAllExpensesSync(): List<Expense>

    @Query("""
        SELECT * FROM expenses 
        WHERE strftime('%m', date/1000, 'unixepoch') = :month 
        AND strftime('%Y', date/1000, 'unixepoch') = :year
        ORDER BY date DESC, id DESC
    """)
    fun getExpensesForMonth(month: String, year: String): LiveData<List<Expense>>

    @Query("""
        SELECT * FROM expenses 
        WHERE strftime('%m', date/1000, 'unixepoch') = :month 
        AND strftime('%Y', date/1000, 'unixepoch') = :year
        ORDER BY date DESC, id DESC
    """)
    suspend fun getExpensesForMonthSync(month: String, year: String): List<Expense>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM expenses 
        WHERE strftime('%m', date/1000, 'unixepoch') = :month 
        AND strftime('%Y', date/1000, 'unixepoch') = :year
    """)
    fun getTotalForMonth(month: String, year: String): LiveData<Double>

    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM expenses 
        WHERE strftime('%m', date/1000, 'unixepoch') = :month 
        AND strftime('%Y', date/1000, 'unixepoch') = :year
    """)
    suspend fun getTotalForMonthSync(month: String, year: String): Double

    @Query("""
        SELECT categoryName, COALESCE(SUM(amount), 0.0) as total 
        FROM expenses 
        WHERE strftime('%m', date/1000, 'unixepoch') = :month 
        AND strftime('%Y', date/1000, 'unixepoch') = :year
        GROUP BY categoryName
        ORDER BY total DESC
    """)
    suspend fun getCategoryTotalsForMonth(month: String, year: String): List<CategoryTotal>

    @Query("""
        SELECT * FROM expenses 
        WHERE (notes LIKE '%' || :query || '%' OR categoryName LIKE '%' || :query || '%')
        ORDER BY date DESC, id DESC
    """)
    fun searchExpenses(query: String): LiveData<List<Expense>>

    @Query("""
        SELECT * FROM expenses 
        WHERE categoryId = :categoryId
        AND strftime('%m', date/1000, 'unixepoch') = :month 
        AND strftime('%Y', date/1000, 'unixepoch') = :year
        ORDER BY date DESC, id DESC
    """)
    fun getExpensesByCategoryAndMonth(categoryId: Int, month: String, year: String): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC LIMIT 5")
    fun getRecentExpenses(): LiveData<List<Expense>>

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Int)
}

data class CategoryTotal(
    val categoryName: String,
    val total: Double
)
