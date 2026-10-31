package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Sale
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
  @Query("SELECT * FROM sales ORDER BY date DESC")
  fun getAllSales(): Flow<List<Sale>>

  @Query("SELECT * FROM sales WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
  fun getSalesBetweenDates(startDate: Long, endDate: Long): Flow<List<Sale>>

  @Query("SELECT * FROM sales")
  suspend fun getAllSalesSync(): List<Sale>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSale(sale: Sale): Long

  @Delete
  suspend fun deleteSale(sale: Sale)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(sales: List<Sale>)

  @Query("DELETE FROM sales")
  suspend fun deleteAll()
}
