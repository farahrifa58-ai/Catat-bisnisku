package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
  @Query("SELECT * FROM expenses ORDER BY date DESC")
  fun getAllExpenses(): Flow<List<Expense>>

  @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
  fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>>

  @Query("SELECT * FROM expenses")
  suspend fun getAllExpensesSync(): List<Expense>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpense(expense: Expense): Long

  @Delete
  suspend fun deleteExpense(expense: Expense)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(expenses: List<Expense>)

  @Query("DELETE FROM expenses")
  suspend fun deleteAll()
}
