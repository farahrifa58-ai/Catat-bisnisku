package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StockMovement
import kotlinx.coroutines.flow.Flow

@Dao
interface StockMovementDao {
  @Query("SELECT * FROM stock_movements ORDER BY date DESC LIMIT 100")
  fun getAllMovements(): Flow<List<StockMovement>>

  @Query("SELECT * FROM stock_movements WHERE itemId = :itemId ORDER BY date DESC")
  fun getMovementsByItem(itemId: Long): Flow<List<StockMovement>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMovement(movement: StockMovement): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(movements: List<StockMovement>)

  @Query("DELETE FROM stock_movements")
  suspend fun deleteAll()
}
