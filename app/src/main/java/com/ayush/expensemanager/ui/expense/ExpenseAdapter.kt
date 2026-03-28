package com.ayush.expensemanager.ui.expense

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ayush.expensemanager.data.entities.Expense
import com.ayush.expensemanager.databinding.ItemExpenseBinding
import com.ayush.expensemanager.utils.CurrencyFormatter

class ExpenseAdapter(
    private val onDelete: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DIFF_CALLBACK) {

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            binding.tvCategory.text = expense.categoryName
            binding.tvAmount.text = CurrencyFormatter.format(expense.amount)
            binding.tvDate.text = CurrencyFormatter.formatDate(expense.date)
            binding.tvNotes.text = expense.notes.ifBlank { "" }
            binding.tvNotes.visibility = if (expense.notes.isBlank()) ViewGroup.GONE else ViewGroup.VISIBLE
            binding.btnDelete.setOnClickListener { onDelete(expense) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Expense>() {
            override fun areItemsTheSame(oldItem: Expense, newItem: Expense) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Expense, newItem: Expense) = oldItem == newItem
        }
    }
}
