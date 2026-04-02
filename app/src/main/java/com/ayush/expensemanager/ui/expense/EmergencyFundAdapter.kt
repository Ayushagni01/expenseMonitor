package com.ayush.expensemanager.ui.expense

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ayush.expensemanager.R
import com.ayush.expensemanager.data.entities.EmergencyFundTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EmergencyFundAdapter(
    private val onDeleteClick: (EmergencyFundTransaction) -> Unit
) : ListAdapter<EmergencyFundTransaction, EmergencyFundAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescription: TextView = view.findViewById(R.id.tv_description)
        val tvAmount: TextView = view.findViewById(R.id.tv_amount)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val ivIcon: ImageView = view.findViewById(R.id.iv_icon)
        val iconContainer: FrameLayout = view.findViewById(R.id.icon_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_emergency_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)

        holder.tvDescription.text = transaction.description
        
        val prefs = holder.itemView.context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val currency = prefs.getString("currency", "₹") ?: "₹"
        
        if (transaction.isCredit) {
            holder.tvAmount.text = "+ $currency ${String.format(Locale.getDefault(), "%.2f", transaction.amount)}"
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50"))
            
            holder.ivIcon.setImageResource(android.R.drawable.ic_input_add)
            holder.ivIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            holder.iconContainer.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#204CAF50"))
        } else {
            holder.tvAmount.text = "- $currency ${String.format(Locale.getDefault(), "%.2f", transaction.amount)}"
            holder.tvAmount.setTextColor(Color.parseColor("#F44336"))
            
            holder.ivIcon.setImageResource(android.R.drawable.ic_delete)
            holder.ivIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#F44336"))
            holder.iconContainer.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#20F44336"))
        }

        val sdf = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
        holder.tvDate.text = sdf.format(Date(transaction.date))

        holder.itemView.setOnLongClickListener {
            onDeleteClick(transaction)
            true
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<EmergencyFundTransaction>() {
            override fun areItemsTheSame(oldItem: EmergencyFundTransaction, newItem: EmergencyFundTransaction): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: EmergencyFundTransaction, newItem: EmergencyFundTransaction): Boolean {
                return oldItem == newItem
            }
        }
    }
}
