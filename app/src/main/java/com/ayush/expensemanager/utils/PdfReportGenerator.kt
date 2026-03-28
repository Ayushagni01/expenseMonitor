package com.ayush.expensemanager.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.ayush.expensemanager.data.dao.CategoryTotal
import com.ayush.expensemanager.data.entities.Expense
import com.ayush.expensemanager.data.entities.Salary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReportGenerator {

    fun generateMonthlyReport(
        context: Context,
        month: Int,
        year: Int,
        salary: Salary?,
        totalSpent: Double,
        expenses: List<Expense>,
        categoryTotals: List<CategoryTotal>,
        currencySymbol: String = "₹"
    ): File? {
        return try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            drawReport(canvas, month, year, salary, totalSpent, expenses, categoryTotals, currencySymbol)

            pdfDocument.finishPage(page)

            // If there are more expenses, create additional pages
            if (expenses.size > 15) {
                val page2Info = PdfDocument.PageInfo.Builder(595, 842, 2).create()
                val page2 = pdfDocument.startPage(page2Info)
                drawExpenseList(page2.canvas, expenses.drop(15), currencySymbol)
                pdfDocument.finishPage(page2)
            }

            val fileName = "ExpenseReport_${getMonthName(month)}_$year.pdf"
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun drawReport(
        canvas: Canvas,
        month: Int,
        year: Int,
        salary: Salary?,
        totalSpent: Double,
        expenses: List<Expense>,
        categoryTotals: List<CategoryTotal>,
        currencySymbol: String
    ) {
        val titlePaint = Paint().apply {
            color = Color.parseColor("#6750A4")
            textSize = 28f
            isFakeBoldText = true
        }
        val headerPaint = Paint().apply {
            color = Color.parseColor("#1C1B1F")
            textSize = 18f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#49454F")
            textSize = 14f
        }
        val accentPaint = Paint().apply {
            color = Color.parseColor("#6750A4")
            textSize = 14f
            isFakeBoldText = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#CAC4D0")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        var y = 60f

        // Title
        canvas.drawText("EXPENSE MANAGER", 40f, y, titlePaint)
        y += 30f
        canvas.drawText("Monthly Report — ${getMonthName(month)} $year", 40f, y, bodyPaint)
        y += 10f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 30f

        // Summary Section
        canvas.drawText("SUMMARY", 40f, y, headerPaint)
        y += 25f

        val salaryAmt = salary?.amount ?: 0.0
        val balance = salaryAmt - totalSpent
        val savingsPercent = if (salaryAmt > 0) ((balance / salaryAmt) * 100).toInt() else 0

        drawKeyValue(canvas, "Monthly Salary:", "$currencySymbol ${formatAmount(salaryAmt)}", 40f, y, bodyPaint, accentPaint)
        y += 22f
        drawKeyValue(canvas, "Total Spent:", "$currencySymbol ${formatAmount(totalSpent)}", 40f, y, bodyPaint, accentPaint)
        y += 22f
        drawKeyValue(canvas, "Remaining Balance:", "$currencySymbol ${formatAmount(balance)}", 40f, y, bodyPaint, accentPaint)
        y += 22f
        drawKeyValue(canvas, "Savings Rate:", "$savingsPercent%", 40f, y, bodyPaint, accentPaint)
        y += 15f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 25f

        // Category Breakdown
        canvas.drawText("CATEGORY BREAKDOWN", 40f, y, headerPaint)
        y += 25f

        categoryTotals.forEach { ct ->
            val percent = if (totalSpent > 0) ((ct.total / totalSpent) * 100).toInt() else 0
            drawKeyValue(canvas, "${ct.categoryName}:", "$currencySymbol ${formatAmount(ct.total)} ($percent%)", 40f, y, bodyPaint, accentPaint)
            y += 20f
        }

        y += 5f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 25f

        // Recent Transactions
        canvas.drawText("TRANSACTIONS (latest 15)", 40f, y, headerPaint)
        y += 25f

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        expenses.take(15).forEach { expense ->
            val dateStr = sdf.format(Date(expense.date))
            val line = "${expense.categoryName.take(20).padEnd(20)}  $dateStr  $currencySymbol ${formatAmount(expense.amount)}"
            canvas.drawText(line, 40f, y, bodyPaint)
            if (expense.notes.isNotBlank()) {
                val notePaint = Paint().apply { color = Color.parseColor("#79747E"); textSize = 12f }
                canvas.drawText("  Note: ${expense.notes.take(60)}", 40f, y + 14f, notePaint)
                y += 14f
            }
            y += 20f
            if (y > 800f) return
        }

        y += 10f
        val footerPaint = Paint().apply { color = Color.parseColor("#79747E"); textSize = 11f }
        canvas.drawText("Generated by Expense Manager • ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())}", 40f, 820f, footerPaint)
    }

    private fun drawExpenseList(canvas: Canvas, expenses: List<Expense>, currencySymbol: String) {
        val bodyPaint = Paint().apply { color = Color.parseColor("#49454F"); textSize = 14f }
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        var y = 60f
        expenses.forEach { expense ->
            canvas.drawText("${expense.categoryName.take(20).padEnd(20)}  ${sdf.format(Date(expense.date))}  $currencySymbol ${formatAmount(expense.amount)}", 40f, y, bodyPaint)
            y += 22f
            if (y > 800f) return
        }
    }

    private fun drawKeyValue(canvas: Canvas, key: String, value: String, x: Float, y: Float, keyPaint: Paint, valuePaint: Paint) {
        canvas.drawText(key, x, y, keyPaint)
        canvas.drawText(value, x + 220f, y, valuePaint)
    }

    private fun formatAmount(amount: Double): String = String.format("%,.2f", amount)

    private fun getMonthName(month: Int): String {
        val months = arrayOf("January","February","March","April","May","June","July","August","September","October","November","December")
        return if (month in 1..12) months[month - 1] else "Unknown"
    }
}
