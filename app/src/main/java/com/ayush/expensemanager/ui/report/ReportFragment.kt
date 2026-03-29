package com.ayush.expensemanager.ui.report

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ayush.expensemanager.databinding.FragmentReportBinding
import com.ayush.expensemanager.utils.CurrencyFormatter
import com.ayush.expensemanager.utils.PdfReportGenerator
import com.ayush.expensemanager.viewmodel.ReportViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ReportViewModel by viewModels()

    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCurrentMonth.text = CurrencyFormatter.getMonthYearLabel(selectedMonth, selectedYear)

        binding.btnPrevMonth.setOnClickListener {
            selectedMonth--
            if (selectedMonth < 1) { selectedMonth = 12; selectedYear-- }
            binding.tvCurrentMonth.text = CurrencyFormatter.getMonthYearLabel(selectedMonth, selectedYear)
            loadReportData()
        }

        binding.btnNextMonth.setOnClickListener {
            selectedMonth++
            if (selectedMonth > 12) { selectedMonth = 1; selectedYear++ }
            binding.tvCurrentMonth.text = CurrencyFormatter.getMonthYearLabel(selectedMonth, selectedYear)
            loadReportData()
        }

        binding.btnGeneratePdf.setOnClickListener { generatePdf() }

        loadReportData()
    }

    private fun loadReportData() {
        lifecycleScope.launch {
            val data = viewModel.getReportData(selectedMonth, selectedYear)
            binding.tvSalary.text = CurrencyFormatter.format(data.salary?.amount ?: 0.0)
            binding.tvTotalSpent.text = CurrencyFormatter.format(data.totalSpent)
            binding.tvBalance.text = CurrencyFormatter.format((data.salary?.amount ?: 0.0) - data.totalSpent)
            binding.tvTransactionCount.text = "${data.expenses.size} transactions"

            val summary = data.categoryTotals.joinToString("\n") { ct ->
                "• ${ct.categoryName}: ${CurrencyFormatter.format(ct.total)}"
            }
            binding.tvCategoryBreakdown.text = summary.ifBlank { "No expenses this month" }
        }
    }

    private fun generatePdf() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val data = viewModel.getReportData(selectedMonth, selectedYear)
            
            val prefs = requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            val pdfUriStr = prefs.getString("pdf_export_uri", null)

            val finalUri = PdfReportGenerator.generateMonthlyReport(
                requireContext(), selectedMonth, selectedYear,
                data.salary, data.totalSpent, data.expenses, data.categoryTotals, "₹", pdfUriStr
            )
            
            binding.progressBar.visibility = View.GONE
            if (finalUri != null) {
                Toast.makeText(requireContext(), "PDF saved successfully!", Toast.LENGTH_LONG).show()
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(finalUri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Open PDF"))
            } else {
                Toast.makeText(requireContext(), "Failed to generate PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
