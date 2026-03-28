package com.ayush.expensemanager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ayush.expensemanager.data.entities.Salary

@Dao
interface SalaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSalary(salary: Salary): Long

    @Query("SELECT * FROM salary WHERE month = :month AND year = :year LIMIT 1")
    fun getSalaryForMonth(month: Int, year: Int): LiveData<Salary?>

    @Query("SELECT * FROM salary WHERE month = :month AND year = :year LIMIT 1")
    suspend fun getSalaryForMonthSync(month: Int, year: Int): Salary?

    @Query("SELECT * FROM salary ORDER BY year DESC, month DESC")
    fun getAllSalaries(): LiveData<List<Salary>>

    @Query("SELECT * FROM salary ORDER BY year DESC, month DESC")
    suspend fun getAllSalariesSync(): List<Salary>

    @Delete
    suspend fun deleteSalary(salary: Salary)
}
