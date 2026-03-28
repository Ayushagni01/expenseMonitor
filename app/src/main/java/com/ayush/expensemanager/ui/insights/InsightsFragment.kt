package com.ayush.expensemanager.ui.insights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ayush.expensemanager.R
import com.ayush.expensemanager.databinding.FragmentInsightsBinding
import com.ayush.expensemanager.utils.CurrencyFormatter
import com.ayush.expensemanager.viewmodel.MainViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import java.util.*

class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()

    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedYear = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCurrentMonth.text = CurrencyFormatter.getMonthYearLabel(selectedMonth, selectedYear)

        binding.btnPrevMonth.setOnClickListener {
            selectedMonth--
            if (selectedMonth < 1) { selectedMonth = 12; selectedYear-- }
            updateView()
        }

        binding.btnNextMonth.setOnClickListener {
            selectedMonth++
            if (selectedMonth > 12) { selectedMonth = 1; selectedYear++ }
            updateView()
        }

        updateView()
    }

    private fun updateView() {
        binding.tvCurrentMonth.text = CurrencyFormatter.getMonthYearLabel(selectedMonth, selectedYear)
        loadInsights()
    }

    private fun loadInsights() {
        lifecycleScope.launch {
            val totals = viewModel.getCategoryTotals(selectedMonth, selectedYear)
            setupPieChart(totals.map { it.categoryName to it.total })
            setupBreakdown(totals.map { it.categoryName to it.total })
        }
    }

    private fun setupPieChart(data: List<Pair<String, Double>>) {
        val entries = data.map { PieEntry(it.second.toFloat(), it.first) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
            valueTextColor = android.graphics.Color.WHITE
        }
        binding.pieChart.apply {
            this.data = PieData(dataSet)
            description.isEnabled = false
            centerText = "Spending"
            setCenterTextSize(18f)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupBreakdown(data: List<Pair<String, Double>>) {
        binding.layoutBreakdown.removeAllViews()
        val total = data.sumOf { it.second }
        
        data.forEach { (name, amount) ->
            val percent = if (total > 0) (amount / total * 100).toInt() else 0
            val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_insight_row, binding.layoutBreakdown, false)
            row.findViewById<TextView>(R.id.tv_category_name).text = name
            row.findViewById<TextView>(R.id.tv_amount).text = CurrencyFormatter.format(amount)
            row.findViewById<TextView>(R.id.tv_percent).text = "$percent%"
            binding.layoutBreakdown.addView(row)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
