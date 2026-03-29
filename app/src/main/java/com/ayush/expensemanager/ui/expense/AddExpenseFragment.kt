package com.ayush.expensemanager.ui.expense

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ayush.expensemanager.R
import com.ayush.expensemanager.data.entities.Category
import com.ayush.expensemanager.data.entities.Expense
import com.ayush.expensemanager.databinding.FragmentAddExpenseBinding
import com.ayush.expensemanager.viewmodel.CategoryViewModel
import com.ayush.expensemanager.viewmodel.ExpenseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseViewModel by activityViewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    private var selectedCategoryId: Int? = null
    private var selectedCategoryName: String = ""
    private var selectedDate: Long = System.currentTimeMillis()
    private var categories = listOf<Category>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDatePicker()
        setupCategoryObserver()
        setupButtons()
    }

    private fun setupDatePicker() {
        val cal = Calendar.getInstance()
        updateDateDisplay(cal)

        binding.btnPickDate.setOnClickListener {
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val c = Calendar.getInstance().apply { set(year, month, dayOfMonth, 12, 0) }
                selectedDate = c.timeInMillis
                updateDateDisplay(c)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun updateDateDisplay(cal: Calendar) {
        binding.tvSelectedDate.text = "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
    }

    private fun setupCategoryObserver() {
        categoryViewModel.allCategories.observe(viewLifecycleOwner) { cats ->
            categories = cats
            val names = cats.map { it.name } + listOf("+ Add New Category")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, names)
            binding.spinnerCategory.setAdapter(adapter)
            binding.spinnerCategory.setOnItemClickListener { parent, _, position, _ ->
                val selectedName = parent.getItemAtPosition(position) as String
                if (selectedName == "+ Add New Category") {
                    showAddCategoryDialog()
                    binding.spinnerCategory.setText("")
                }
            }
        }
    }

    private fun showAddCategoryDialog() {
        val input = TextInputEditText(requireContext()).apply {
            hint = "Category name"
            setPadding(48, 32, 48, 32)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("New Category")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotBlank()) {
                    categoryViewModel.addCategory(name)
                    Toast.makeText(requireContext(), "Category '$name' added", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener { saveExpense() }
        binding.btnCancel.setOnClickListener { findNavController().navigateUp() }
    }

    private fun saveExpense() {
        val amountStr = binding.etAmount.text.toString().trim()
        if (amountStr.isBlank()) {
            binding.etAmount.error = "Amount required"
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.etAmount.error = "Enter a valid amount"
            return
        }

        val inputCategoryName = binding.spinnerCategory.text.toString().trim()
        if (inputCategoryName.isBlank() || inputCategoryName == "+ Add New Category") {
            binding.spinnerCategory.error = "Category required"
            Toast.makeText(requireContext(), "Please select or type a category", Toast.LENGTH_SHORT).show()
            return
        }

        val matchedCategory = categories.find { it.name.equals(inputCategoryName, ignoreCase = true) }
        val finalCategoryName = matchedCategory?.name ?: inputCategoryName
        val finalCategoryId = matchedCategory?.id

        if (matchedCategory == null) {
            // Auto-add new category since user typed it directly
            categoryViewModel.addCategory(finalCategoryName)
        }

        val expense = Expense(
            amount = amount,
            categoryId = finalCategoryId,
            categoryName = finalCategoryName,
            notes = binding.etNotes.text.toString().trim(),
            date = selectedDate,
            isRecurring = binding.cbRecurring.isChecked
        )
        viewModel.addExpense(expense)
        Toast.makeText(requireContext(), "Expense saved!", Toast.LENGTH_SHORT).show()
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
