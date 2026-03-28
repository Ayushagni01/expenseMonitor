package com.ayush.expensemanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "salary")
data class Salary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val month: Int,  // 1-12
    val year: Int,
    val createdAt: Long = System.currentTimeMillis()
)
