package com.ayush.expensemanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ayush.expensemanager.data.dao.CategoryDao
import com.ayush.expensemanager.data.dao.ExpenseDao
import com.ayush.expensemanager.data.dao.SalaryDao
import com.ayush.expensemanager.data.entities.Category
import com.ayush.expensemanager.data.entities.Expense
import com.ayush.expensemanager.data.entities.Salary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Salary::class, Category::class, Expense::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun salaryDao(): SalaryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_manager_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
