package com.ayush.expensemanager.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.ayush.expensemanager.data.entities.Category
import com.ayush.expensemanager.data.entities.Expense
import com.ayush.expensemanager.data.entities.Salary
import com.ayush.expensemanager.data.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class ImportResult(
    val categoriesImported: Int = 0,
    val salariesImported: Int = 0,
    val expensesImported: Int = 0,
    val emergencyFundsImported: Int = 0,
    val errors: List<String> = emptyList()
)

object DataBackupUtils {

    suspend fun exportToCsv(
        expenses: List<Expense>,
        categories: List<Category>,
        salaries: List<Salary>,
        emergencyFunds: List<com.ayush.expensemanager.data.entities.EmergencyFundTransaction>
    ): String = withContext(Dispatchers.IO) {
        val builder = StringBuilder()

        builder.append("TYPE,ID,FIELD1,FIELD2,FIELD3,FIELD4\n")

        categories.forEach {
            builder.append("CATEGORY,${it.id},\"${it.name}\",${it.colorHex},,\n")
        }

        salaries.forEach {
            builder.append("SALARY,${it.id},${it.amount},${it.month},${it.year},\n")
        }

        expenses.forEach {
            builder.append("EXPENSE,${it.id},${it.amount},${it.date},\"${it.categoryName}\",\"${it.notes}\"\n")
        }

        emergencyFunds.forEach {
            builder.append("EMERGENCY_FUND,${it.id},${it.amount},${it.isCredit},\"${it.description}\",${it.date}\n")
        }

        builder.toString()
    }

    suspend fun importFromCsv(
        context: Context,
        uri: Uri,
        repository: ExpenseRepository
    ): ImportResult = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Cannot open input stream for URI: $uri")
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        
        var categoriesCount = 0
        var salariesCount = 0
        var expensesCount = 0
        var emergencyFundsCount = 0
        val errors = mutableListOf<String>()

        var line: String? = reader.readLine()
        // Strip BOM if present
        if (line != null && line.startsWith("\uFEFF")) {
            line = line.substring(1)
        }

        while (line != null) {
            val parts = splitCsv(line!!)
            if (parts.isNotEmpty() && parts[0] != "TYPE") {
                try {
                    when (parts[0]) {
                        "CATEGORY" -> {
                            if (parts.size >= 4) {
                                repository.insertCategory(Category(name = parts[2], colorHex = parts[3]))
                                categoriesCount++
                            }
                        }
                        "SALARY" -> {
                            if (parts.size >= 5) {
                                repository.insertSalary(Salary(
                                    amount = parts[2].toDoubleOrNull() ?: 0.0,
                                    month = parts[3].toIntOrNull() ?: 1,
                                    year = parts[4].toIntOrNull() ?: 2026
                                ))
                                salariesCount++
                            }
                        }
                        "EXPENSE" -> {
                            if (parts.size >= 6) {
                                repository.insertExpense(Expense(
                                    amount = parts[2].toDoubleOrNull() ?: 0.0,
                                    date = parts[3].toLongOrNull() ?: System.currentTimeMillis(),
                                    categoryName = parts[4],
                                    categoryId = null,
                                    notes = parts[5]
                                ))
                                expensesCount++
                            }
                        }
                        "EMERGENCY_FUND" -> {
                            if (parts.size >= 6) {
                                repository.insertEmergencyFundTransaction(
                                    com.ayush.expensemanager.data.entities.EmergencyFundTransaction(
                                        amount = parts[2].toDoubleOrNull() ?: 0.0,
                                        isCredit = parts[3].toBoolean(),
                                        description = parts[4],
                                        date = parts[5].toLongOrNull() ?: System.currentTimeMillis()
                                    )
                                )
                                emergencyFundsCount++
                            }
                        }
                    }
                } catch (e: Exception) {
                    val errorMsg = "Error parsing line: $line - ${e.message}"
                    Log.e("DataBackupUtils", errorMsg)
                    errors.add(errorMsg)
                }
            }
            line = reader.readLine()
        }
        reader.close()
        ImportResult(categoriesCount, salariesCount, expensesCount, emergencyFundsCount, errors)
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
