package com.ayush.expensemanager.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ayush.expensemanager.data.entities.Category
import com.ayush.expensemanager.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onDelete: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(DIFF_CALLBACK) {

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name
            try {
                val color = android.graphics.Color.parseColor(category.colorHex)
                binding.viewColorDot.setBackgroundColor(color)
            } catch (e: Exception) {
                binding.viewColorDot.setBackgroundColor(android.graphics.Color.parseColor("#6750A4"))
            }
            binding.btnDelete.setOnClickListener { onDelete(category) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Category, newItem: Category) = oldItem == newItem
        }
    }
}
