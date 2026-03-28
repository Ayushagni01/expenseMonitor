package com.ayush.expensemanager.ui.salary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.ayush.expensemanager.databinding.FragmentSetSalaryBinding
import com.ayush.expensemanager.viewmodel.MainViewModel
import java.util.Calendar

class SetSalaryFragment : Fragment() {

    private var _binding: FragmentSetSalaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSetSalaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH) + 1
        val currentYear = cal.get(Calendar.YEAR)

        // Pre-fill months spinner
        val months = (1..12).map { month ->
            val c = Calendar.getInstance().apply { set(Calendar.MONTH, month - 1) }
            java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault()).format(c.time)
        }
        val monthAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, months)
        binding.spinnerMonth.setAdapter(monthAdapter)
        binding.spinnerMonth.setText(months[currentMonth - 1], false)
        binding.etYear.setText(currentYear.toString())

        // Load existing salary if any
        viewModel.currentSalary.observe(viewLifecycleOwner) { salary ->
            salary?.let { binding.etSalary.setText(it.amount.toString()) }
        }

        binding.btnSave.setOnClickListener {
            val amountStr = binding.etSalary.text.toString().trim()
            val yearStr = binding.etYear.text.toString().trim()
            val monthIdx = months.indexOf(binding.spinnerMonth.text.toString())

            if (amountStr.isBlank()) {
                binding.etSalary.error = "Enter salary amount"
                return@setOnClickListener
            }
            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                binding.etSalary.error = "Enter a valid amount"
                return@setOnClickListener
            }
            val year = yearStr.toIntOrNull() ?: currentYear
            val month = if (monthIdx >= 0) monthIdx + 1 else currentMonth

            viewModel.saveSalary(amount, month, year)
            Toast.makeText(requireContext(), "Salary saved!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
