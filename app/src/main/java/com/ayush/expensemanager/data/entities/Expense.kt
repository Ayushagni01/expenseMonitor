package com.ayush.expensemanager.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Double,
    val categoryId: Int?,
    val categoryName: String,
    val notes: String = "",
    val date: Long,  // timestamp (milliseconds)
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
