package com.ayush.expensemanager.utils

import android.content.Context
import android.net.Uri
import com.ayush.expensemanager.data.entities.Category
import com.ayush.expensemanager.data.entities.Expense
import com.ayush.expensemanager.data.entities.Salary
import com.ayush.expensemanager.data.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object DataBackupUtils {

    suspend fun exportToCsv(
        expenses: List<Expense>,
        categories: List<Category>,
        salaries: List<Salary>
    ): String = withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        
        // Categories Header: TYPE,ID,NAME,COLOR
        builder.append("TYPE,ID,NAME,COLOR\n")
        categories.forEach { 
            builder.append("CATEGORY,${it.id},\"${it.name}\",${it.colorHex}\n")
        }

        // Salaries Header: TYPE,ID,AMOUNT,MONTH,YEAR
        builder.append("TYPE,ID,AMOUNT,MONTH,YEAR\n")
        salaries.forEach {
            builder.append("SALARY,${it.id},${it.amount},${it.month},${it.year}\n")
        }

        // Expenses Header: TYPE,ID,AMOUNT,DATE,CATEGORY_NAME,NOTES
        builder.append("TYPE,ID,AMOUNT,DATE,CATEGORY_NAME,NOTES\n")
        expenses.forEach {
            builder.append("EXPENSE,${it.id},${it.amount},${it.date},\"${it.categoryName}\",\"${it.notes}\"\n")
        }

        builder.toString()
    }

    suspend fun importFromCsv(
        context: Context,
        uri: Uri,
        repository: ExpenseRepository
    ) = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        
        // Skip header lines or handle dynamically
        while (reader.readLine().also { line = it } != null) {
            val parts = splitCsv(line!!)
            if (parts.isEmpty() || parts[0] == "TYPE") continue

            try {
                when (parts[0]) {
                    "CATEGORY" -> {
                        if (parts.size >= 4) {
                            repository.insertCategory(Category(name = parts[2], colorHex = parts[3]))
                        }
                    }
                    "SALARY" -> {
                        if (parts.size >= 5) {
                            repository.insertSalary(Salary(
                                amount = parts[2].toDoubleOrNull() ?: 0.0,
                                month = parts[3].toIntOrNull() ?: 1,
                                year = parts[4].toIntOrNull() ?: 2026
                            ))
                        }
                    }
                    "EXPENSE" -> {
                        if (parts.size >= 6) {
                            repository.insertExpense(Expense(
                                amount = parts[2].toDoubleOrNull() ?: 0.0,
                                date = parts[3].toLongOrNull() ?: System.currentTimeMillis(),
                                categoryName = parts[4],
                                categoryId = null, // Will be set to NULL due to SET_NULL foreign key or manual linking later
                                notes = parts[5]
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                // Skip malformed lines
            }
        }
        reader.close()
    }

    private fun splitCsv(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var inQuotes = false
        var current = StringBuilder()
        
        for (char in line) {
            when (char) {
                '\"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) {
                        current.append(char)
                    } else {
                        tokens.add(current.toString().trim())
                        current = StringBuilder()
                    }
                }
                else -> current.append(char)
            }
        }
        tokens.add(current.toString().trim())
        return tokens
    }
}
