package com.ayush.expensemanager.utils

import java.text.SimpleDateFormat
import java.util.*

object CurrencyFormatter {
    fun format(amount: Double, symbol: String = "₹"): String {
        return "$symbol ${String.format("%,.2f", amount)}"
    }

    fun getMonthName(month: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, month - 1)
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
    }

    fun getMonthYearLabel(month: Int, year: Int): String {
        return "${getMonthName(month)} $year"
    }

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        return SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(timestamp))
    }
}
