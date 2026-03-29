package com.ayush.expensemanager.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ayush.expensemanager.R
import com.ayush.expensemanager.databinding.FragmentDashboardBinding
import com.ayush.expensemanager.ui.expense.ExpenseAdapter
import com.ayush.expensemanager.utils.CurrencyFormatter
import com.ayush.expensemanager.utils.NotificationHelper
import com.ayush.expensemanager.viewmodel.MainViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeData()

        val cal = Calendar.getInstance()
        binding.tvMonthYear.text = CurrencyFormatter.getMonthYearLabel(
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        )
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter { expense ->
            viewModel.deleteExpense(expense)
        }
        binding.rvRecentExpenses.apply {
            adapter = expenseAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.fabAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addExpense)
        }
        binding.btnSetSalary.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_setSalary)
        }
        binding.layoutSeeAll.setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = R.id.expenseHistoryFragment
        }
    }

    private fun observeData() {
        viewModel.currentSalary.observe(viewLifecycleOwner) { salary ->
            val amount = salary?.amount ?: 0.0
            binding.tvSalaryAmount.text = CurrencyFormatter.format(amount)
        }

        viewModel.totalSpent.observe(viewLifecycleOwner) { spent ->
            binding.tvSpentAmount.text = CurrencyFormatter.format(spent)
        }

        viewModel.currentSalary.observe(viewLifecycleOwner) { salary ->
            val salaryAmt = salary?.amount ?: 0.0
            val spent = viewModel.totalSpent.value ?: 0.0
            val balance = salaryAmt - spent
            binding.tvBalanceAmount.text = CurrencyFormatter.format(balance)

            // Progress bar
            val percent = if (salaryAmt > 0) ((spent / salaryAmt) * 100).toInt() else 0
            binding.progressBudget.progress = percent.coerceIn(0, 100)
            binding.tvBudgetPercent.text = "$percent% used"

            // Alert check
            if (NotificationHelper.shouldSendAlert(spent, salaryAmt)) {
                NotificationHelper.sendBudgetAlert(requireContext(), "₹", spent, salaryAmt)
            }
        }

        viewModel.totalSpent.observe(viewLifecycleOwner) {
            updateBalance()
        }

        viewModel.monthlyExpenses.observe(viewLifecycleOwner) { expenses ->
            val displayList = expenses.take(5)
            expenseAdapter.submitList(displayList)
            binding.tvNoExpenses.visibility = if (displayList.isEmpty()) View.VISIBLE else View.GONE
            binding.layoutSeeAll.visibility = if (expenses.size > 5) View.VISIBLE else View.GONE
        }
    }

    private fun updateBalance() {
        val salary = viewModel.currentSalary.value?.amount ?: 0.0
        val spent = viewModel.totalSpent.value ?: 0.0
        binding.tvBalanceAmount.text = CurrencyFormatter.format(salary - spent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
