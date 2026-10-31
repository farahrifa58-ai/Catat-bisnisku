package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemType {
  PRODUCT,      // Barang dagangan (Jual Beli) atau Produk Jadi (Produksi)
  RAW_MATERIAL  // Bahan Baku (Produksi)
}

@Entity(tableName = "items")
data class Item(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val itemType: String = ItemType.PRODUCT.name,
  val stock: Double = 0.0,
  val unit: String = "Pcs",
  val costPrice: Double = 0.0,       // Modal per unit
  val sellingPrice: Double = 0.0,    // Harga jual per unit
  val minStockAlert: Double = 5.0,   // Peringatan stok menipis
  val imageUri: String? = null,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
) {
  val isLowStock: Boolean
    get() = stock <= minStockAlert
    
  val isOutOfStock: Boolean
    get() = stock <= 0.0
    
  val totalStockValue: Double
    get() = stock * costPrice
}
