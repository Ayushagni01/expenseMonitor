package com.ayush.expensemanager.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.ayush.expensemanager.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "budget_alert_channel"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        val name = "Budget Alerts"
        val descriptionText = "Notifications when spending exceeds 80% of monthly budget"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun sendBudgetAlert(context: Context, currencySymbol: String, spent: Double, salary: Double) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val percent = if (salary > 0) ((spent / salary) * 100).toInt() else 0
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Budget Alert!")
            .setContentText("You've spent $percent% ($currencySymbol${String.format("%,.0f", spent)}) of your monthly budget!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("You've used $percent% of your monthly budget.\nSpent: $currencySymbol${String.format("%,.0f", spent)} / $currencySymbol${String.format("%,.0f", salary)}\nRemaining: $currencySymbol${String.format("%,.0f", salary - spent)}")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun shouldSendAlert(spent: Double, salary: Double): Boolean {
        if (salary <= 0) return false
        val percent = (spent / salary) * 100
        return percent >= 80
    }

    fun showNotification(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
}
