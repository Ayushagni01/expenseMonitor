package com.ayush.expensemanager.ui.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ayush.expensemanager.R
import com.ayush.expensemanager.viewmodel.EmergencyFundViewModel
import java.util.Locale

class EmergencyFundFragment : Fragment() {

    private val viewModel: EmergencyFundViewModel by activityViewModels()
    private lateinit var adapter: EmergencyFundAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_emergency_fund, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvTransactions = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_transactions)
        val tvTotalBalance = view.findViewById<android.widget.TextView>(R.id.tv_total_balance)
        val fabAdd = view.findViewById<View>(R.id.fab_add_transaction)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)

        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        adapter = EmergencyFundAdapter { transaction ->
            viewModel.deleteTransaction(transaction)
        }
        rvTransactions.layoutManager = LinearLayoutManager(requireContext())
        rvTransactions.adapter = adapter

        val prefs = requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val currency = prefs.getString("currency", "₹")

        viewModel.totalBalance.observe(viewLifecycleOwner) { balance ->
            tvTotalBalance.text = "$currency ${String.format(Locale.getDefault(), "%.2f", balance ?: 0.0)}"
        }

        viewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            adapter.submitList(transactions)
        }

        fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_emergencyFund_to_addEmergencyFund)
        }
    }
}
