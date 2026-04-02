package com.ayush.expensemanager.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ayush.expensemanager.data.entities.EmergencyFundTransaction

@Dao
interface EmergencyFundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: EmergencyFundTransaction): Long

    @Delete
    suspend fun deleteTransaction(transaction: EmergencyFundTransaction)

    @Query("SELECT * FROM emergency_fund ORDER BY date DESC, id DESC")
    fun getAllTransactions(): LiveData<List<EmergencyFundTransaction>>

    @Query("SELECT * FROM emergency_fund ORDER BY date DESC, id DESC")
    suspend fun getAllTransactionsSync(): List<EmergencyFundTransaction>

    @Query("SELECT COALESCE(SUM(CASE WHEN isCredit = 1 THEN amount ELSE -amount END), 0.0) FROM emergency_fund")
    fun getTotalBalance(): LiveData<Double>
}
