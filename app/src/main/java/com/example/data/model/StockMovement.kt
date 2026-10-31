package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MovementType(val title: String) {
  IN_PURCHASE("Tambah Stok"),
  OUT_ADJUSTMENT("Kurangi Stok"),
  PRODUCTION_INPUT("Digunakan Produksi"),
  PRODUCTION_OUTPUT("Hasil Produksi"),
  SALE("Terjual")
}

@Entity(tableName = "stock_movements")
data class StockMovement(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val itemId: Long,
  val itemName: String,
  val movementType: String,
  val qty: Double,
  val costPerUnit: Double = 0.0,
  val note: String = "",
  val date: Long = System.currentTimeMillis()
)
