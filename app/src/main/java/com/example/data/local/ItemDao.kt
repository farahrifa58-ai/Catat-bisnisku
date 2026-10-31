package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
  @Query("SELECT * FROM items ORDER BY name ASC")
  fun getAllItems(): Flow<List<Item>>

  @Query("SELECT * FROM items WHERE itemType = :itemType ORDER BY name ASC")
  fun getItemsByType(itemType: String): Flow<List<Item>>

  @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
  fun getItemById(id: Long): Flow<Item?>

  @Query("SELECT * FROM items WHERE id = :id LIMIT 1")
  suspend fun getItemByIdSync(id: Long): Item?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: Item): Long

  @Update
  suspend fun updateItem(item: Item)

  @Delete
  suspend fun deleteItem(item: Item)

  @Query("UPDATE items SET stock = :newStock, costPrice = :newCostPrice, updatedAt = :updatedAt WHERE id = :id")
  suspend fun updateStockAndCost(id: Long, newStock: Double, newCostPrice: Double, updatedAt: Long)

  @Query("UPDATE items SET stock = :newStock, updatedAt = :updatedAt WHERE id = :id")
  suspend fun updateStock(id: Long, newStock: Double, updatedAt: Long)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(items: List<Item>)

  @Query("DELETE FROM items")
  suspend fun deleteAll()
}
