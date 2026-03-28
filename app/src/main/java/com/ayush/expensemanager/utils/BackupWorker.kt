package com.ayush.expensemanager.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ayush.expensemanager.data.AppDatabase
import com.ayush.expensemanager.data.repository.ExpenseRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = ExpenseRepository(db.salaryDao(), db.categoryDao(), db.expenseDao())

            val expenses = repository.getAllExpensesSync()
            val categories = repository.getAllCategoriesSync()
            val salaries = repository.getAllSalariesSync()

            val csvData = DataBackupUtils.exportToCsv(expenses, categories, salaries)

            val prefs = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val backupUriStr = prefs.getString("backup_uri", null)

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
            val fileName = "auto_backup_$timeStamp.csv"

            if (backupUriStr != null) {
                // Save to User-Selected Folder via SAF
                val treeUri = Uri.parse(backupUriStr)
                val pickedDir = DocumentFile.fromTreeUri(applicationContext, treeUri)
                
                val file = pickedDir?.createFile("text/csv", fileName)
                file?.uri?.let { uri ->
                    applicationContext.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(csvData.toByteArray())
                    }
                }
            } else {
                // Save to Default Directory (Legacy/Fallback)
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val backupDir = File(documentsDir, "eXpense-Backups")
                if (!backupDir.exists()) {
                    backupDir.mkdirs()
                }
                val backupFile = File(backupDir, fileName)
                backupFile.writeText(csvData)
            }

            NotificationHelper.showNotification(
                applicationContext,
                "Auto Backup Successful",
                "Your daily backup has been saved."
            )

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
