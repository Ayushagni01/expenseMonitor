package com.ayush.expensemanager.ui.settings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.ayush.expensemanager.data.AppDatabase
import com.ayush.expensemanager.data.repository.ExpenseRepository
import com.ayush.expensemanager.databinding.FragmentSettingsBinding
import android.content.Intent
import com.ayush.expensemanager.utils.DataBackupUtils
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.ayush.expensemanager.utils.BackupWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: ExpenseRepository

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("*/*")) { uri ->
        uri?.let { saveCsvToUri(it) }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { loadCsvFromUri(it) }
    }

    private val folderPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { handleFolderSelected(it) }
    }

    private val pdfFolderPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let { handlePdfFolderSelected(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getDatabase(requireContext())
        repository = ExpenseRepository(db.salaryDao(), db.categoryDao(), db.expenseDao(), db.emergencyFundDao())

        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        binding.switchDarkMode.isChecked = isDarkMode

        binding.etCurrency.setText(prefs.getString("currency", "₹"))

        // Load backup time
        val hour = prefs.getInt("backup_hour", -1)
        val minute = prefs.getInt("backup_minute", -1)
        if (hour != -1 && minute != -1) {
            binding.tvBackupTime.text = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        }

        // Load backup folder
        val backupUri = prefs.getString("backup_uri", null)
        if (backupUri != null) {
            binding.tvBackupFolder.text = "Custom Folder Selected"
        }

        // Load PDF folder
        val pdfUri = prefs.getString("pdf_export_uri", null)
        if (pdfUri != null) {
            binding.tvPdfFolder.text = "Custom Folder Selected"
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.layoutBackupTime.setOnClickListener {
            showTimePicker(prefs)
        }

        binding.layoutBackupFolder.setOnClickListener {
            folderPickerLauncher.launch(null)
        }

        binding.layoutPdfFolder.setOnClickListener {
            pdfFolderPickerLauncher.launch(null)
        }

        binding.btnSaveSettings.setOnClickListener {
            val currency = binding.etCurrency.text.toString().trim().ifBlank { "₹" }
            prefs.edit().putString("currency", currency).apply()
            Toast.makeText(requireContext(), "Settings saved!", Toast.LENGTH_SHORT).show()
        }

        binding.btnCloudBackup.setOnClickListener {
            shareBackupFile()
        }

        binding.btnExportCsv.setOnClickListener {
            exportLauncher.launch("expense_backup_${System.currentTimeMillis()}.csv")
        }

        binding.btnImportCsv.setOnClickListener {
            importLauncher.launch("*/*")
        }
    }

    private fun saveCsvToUri(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val expenses = repository.getAllExpensesSync()
                val categories = repository.getAllCategoriesSync()
                val salaries = repository.getAllSalariesSync()
                val emergencyFunds = repository.getAllEmergencyFundTransactionsSync()
                
                val csvData = DataBackupUtils.exportToCsv(expenses, categories, salaries, emergencyFunds)
                
                requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvData.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(requireContext(), "Data exported successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadCsvFromUri(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = DataBackupUtils.importFromCsv(requireContext(), uri, repository)
                val total = result.expensesImported + result.categoriesImported + result.salariesImported + result.emergencyFundsImported

                if (total == 0) {
                    val errorHint = if (result.errors.isNotEmpty())
                        "\nError: ${result.errors.first()}"
                    else
                        "\nNo valid data rows found in the file."
                    Toast.makeText(
                        requireContext(),
                        "Import finished but 0 records were loaded.$errorHint",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val msg = buildString {
                        append("✅ Imported successfully!\n")
                        if (result.expensesImported > 0)  append("• ${result.expensesImported} expenses\n")
                        if (result.categoriesImported > 0) append("• ${result.categoriesImported} categories\n")
                        if (result.salariesImported > 0)  append("• ${result.salariesImported} salaries\n")
                        if (result.emergencyFundsImported > 0) append("• ${result.emergencyFundsImported} emergency funds\n")
                        append("Showing in Expense History →")
                    }
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

                    // Navigate to Expense History so user can see ALL imported data
                    // (Dashboard only shows current month — old expenses would be invisible there)
                    activity?.findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                        com.ayush.expensemanager.R.id.bottom_navigation
                    )?.selectedItemId = com.ayush.expensemanager.R.id.expenseHistoryFragment
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun shareBackupFile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val expenses = repository.getAllExpensesSync()
                val categories = repository.getAllCategoriesSync()
                val salaries = repository.getAllSalariesSync()
                val emergencyFunds = repository.getAllEmergencyFundTransactionsSync()
                
                val csvData = DataBackupUtils.exportToCsv(expenses, categories, salaries, emergencyFunds)
                
                val cacheFile = File(requireContext().cacheDir, "expense_backup.csv")
                cacheFile.writeText(csvData)
                
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    cacheFile
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                startActivity(Intent.createChooser(intent, "Backup to Cloud"))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showTimePicker(prefs: android.content.SharedPreferences) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(if (prefs.getInt("backup_hour", -1) != -1) prefs.getInt("backup_hour", 10) else 10)
            .setMinute(if (prefs.getInt("backup_minute", -1) != -1) prefs.getInt("backup_minute", 0) else 0)
            .setTitleText("Select Backup Time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val h = picker.hour
            val m = picker.minute
            prefs.edit().putInt("backup_hour", h).putInt("backup_minute", m).apply()
            binding.tvBackupTime.text = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            scheduleBackup(h, m)
            Toast.makeText(requireContext(), "Daily backup scheduled at $h:$m", Toast.LENGTH_SHORT).show()
        }

        picker.show(childFragmentManager, "time_picker")
    }

    private fun scheduleBackup(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val delay = calendar.timeInMillis - System.currentTimeMillis()
        
        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag("daily_backup")
            .build()
            
        WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
            "daily_backup_work",
            ExistingPeriodicWorkPolicy.UPDATE,
            backupRequest
        )
    }

    private fun handleFolderSelected(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        requireContext().contentResolver.takePersistableUriPermission(uri, flags)
        
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("backup_uri", uri.toString()).apply()
        
        binding.tvBackupFolder.text = "Custom Folder Selected"
        Toast.makeText(requireContext(), "Backup folder updated!", Toast.LENGTH_SHORT).show()
    }

    private fun handlePdfFolderSelected(uri: Uri) {
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        requireContext().contentResolver.takePersistableUriPermission(uri, flags)
        
        val prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        prefs.edit().putString("pdf_export_uri", uri.toString()).apply()
        
        binding.tvPdfFolder.text = "Custom Folder Selected"
        Toast.makeText(requireContext(), "PDF Export folder updated!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
