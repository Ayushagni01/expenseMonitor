package com.ayush.expensemanager.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.ayush.expensemanager.databinding.FragmentCategoryBinding
import com.ayush.expensemanager.viewmodel.CategoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryAdapter = CategoryAdapter { category ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Category")
                .setMessage("Delete '${category.name}'? Expenses in this category will be kept.")
                .setPositiveButton("Delete") { _, _ -> viewModel.deleteCategory(category) }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.fabAddCategory.setOnClickListener { showAddCategoryDialog() }

        viewModel.allCategories.observe(viewLifecycleOwner) { cats ->
            categoryAdapter.submitList(cats)
            binding.tvEmpty.visibility = if (cats.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showAddCategoryDialog() {
        val bottomSheet = AddCategoryBottomSheet { name ->
            viewModel.addCategory(name)
            Toast.makeText(requireContext(), "'$name' added", Toast.LENGTH_SHORT).show()
        }
        bottomSheet.show(childFragmentManager, AddCategoryBottomSheet.TAG)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
