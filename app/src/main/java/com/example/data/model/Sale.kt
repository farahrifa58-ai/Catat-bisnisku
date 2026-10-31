package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val invoiceNumber: String,
  val itemId: Long,
  val itemName: String,
  val qty: Double,
  val unit: String = "Pcs",
  val sellingPrice: Double,
  val discount: Double = 0.0,
  val totalPrice: Double,           // (qty * sellingPrice) - discount
  val unitCostPrice: Double = 0.0,  // Modal per unit saat penjualan
  val totalCost: Double = 0.0,      // qty * unitCostPrice (Modal Terjual)
  val grossProfit: Double = 0.0,    // totalPrice - totalCost (Keuntungan Kotor)
  val customerName: String = "",
  val paymentMethod: String = "Tunai",
  val notes: String = "",
  val date: Long = System.currentTimeMillis()
)
