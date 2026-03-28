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
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDefaultCategories(database.categoryDao())
                    }
                }
            }
        }

        private suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
            val defaultCategories = listOf(
                Category(name = "Food & Dining", colorHex = "#FF5722"),
                Category(name = "Transportation", colorHex = "#2196F3"),
                Category(name = "Shopping", colorHex = "#E91E63"),
                Category(name = "Entertainment", colorHex = "#9C27B0"),
                Category(name = "Healthcare", colorHex = "#4CAF50"),
                Category(name = "Utilities", colorHex = "#FF9800"),
                Category(name = "Education", colorHex = "#00BCD4"),
                Category(name = "Others", colorHex = "#607D8B")
            )
            defaultCategories.forEach { categoryDao.insertCategory(it) }
        }
    }
}
