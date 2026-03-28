package com.ayush.expensemanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val colorHex: String = "#6750A4",
    val iconName: String = "ic_category",
    val createdAt: Long = System.currentTimeMillis()
)
