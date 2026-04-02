package com.ayush.expensemanager.ui.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.ayush.expensemanager.R
import com.ayush.expensemanager.data.entities.EmergencyFundTransaction
import com.ayush.expensemanager.viewmodel.EmergencyFundViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText

class AddEmergencyFundBottomSheet : BottomSheetDialogFragment() {

    private val viewModel: EmergencyFundViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_add_emergency, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleGroup)
        val etAmount = view.findViewById<TextInputEditText>(R.id.et_amount)
        val etDescription = view.findViewById<TextInputEditText>(R.id.et_description)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        toggleGroup.check(R.id.btn_credit)

        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val isCredit = toggleGroup.checkedButtonId == R.id.btn_credit

            if (amountStr.isEmpty()) {
                etAmount.error = "Enter amount"
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                etAmount.error = "Enter valid amount"
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                etDescription.error = "Enter description"
                return@setOnClickListener
            }

            val transaction = EmergencyFundTransaction(
                amount = amount,
                isCredit = isCredit,
                description = description
            )

            viewModel.addTransaction(transaction)
            Toast.makeText(requireContext(), "Transaction added", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}
