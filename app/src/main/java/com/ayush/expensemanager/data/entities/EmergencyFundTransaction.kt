package com.ayush.expensemanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_fund")
data class EmergencyFundTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val isCredit: Boolean,
    val description: String,
    val date: Long = System.currentTimeMillis()
)
