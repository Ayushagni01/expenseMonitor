package com.ayush.expensemanager.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ayush.expensemanager.databinding.BottomSheetAddCategoryBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddCategoryBottomSheet(
    private val onCategoryAdded: (String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddCategory.setOnClickListener {
            val name = binding.etCategoryName.text.toString().trim()
            if (name.isNotBlank()) {
                onCategoryAdded(name)
                dismiss()
            } else {
                binding.tilCategoryName.error = "Name cannot be empty"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AddCategoryBottomSheet"
    }
}
