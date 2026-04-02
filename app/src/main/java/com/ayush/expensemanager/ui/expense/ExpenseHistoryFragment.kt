package com.ayush.expensemanager.ui.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ayush.expensemanager.data.entities.Category
import com.ayush.expensemanager.databinding.FragmentExpenseHistoryBinding
import com.ayush.expensemanager.viewmodel.ExpenseViewModel
import java.util.Calendar

class ExpenseHistoryFragment : Fragment() {

    private var _binding: FragmentExpenseHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by activityViewModels()
    private lateinit var expenseAdapter: ExpenseAdapter
    private var categories = listOf<Category>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExpenseHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupMonthFilter()
        observeData()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter { expense ->
            viewModel.deleteExpense(expense)
        }
        binding.rvExpenses.apply {
            adapter = expenseAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchExpenses(newText ?: "")
                return true
            }
        })
    }

    private fun setupMonthFilter() {
        val cal = Calendar.getInstance()
        // "All Time" as first entry so imported data from any month is visible
        val allOption = "All Time"
        val months = listOf(allOption) + (1..12).map { month ->
            val c = Calendar.getInstance().apply { set(Calendar.MONTH, month - 1) }
            java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault()).format(c.time)
        }
        val monthAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, months)
        binding.spinnerMonth.setAdapter(monthAdapter)
        // Default: show all
        binding.spinnerMonth.setText(allOption, false)

        binding.spinnerMonth.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position == 0) {
                // "All Time" — show everything
                viewModel.searchExpenses("")
            } else {
                val month = String.format("%02d", position) // position 1 = January = month 01
                val year = cal.get(Calendar.YEAR).toString()
                viewModel.getExpensesForMonth(month, year).observe(viewLifecycleOwner) { expenses ->
                    expenseAdapter.submitList(expenses)
                    binding.tvEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun observeData() {
        // On initial load shows ALL expenses (searchResults with blank query = all expenses)
        viewModel.searchResults.observe(viewLifecycleOwner) { expenses ->
            expenseAdapter.submitList(expenses)
            binding.tvEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
