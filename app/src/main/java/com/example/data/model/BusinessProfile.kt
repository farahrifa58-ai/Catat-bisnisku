package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BusinessType(val label: String, val description: String) {
  JUAL_BELI("Usaha Jual Beli", "Toko, Warung, Reseller, Pedagang (Barang Masuk → Penjualan)"),
  PRODUKSI("Usaha Produksi", "Kue, Makanan, Minuman, Konveksi (Bahan Baku → Produksi → Produk Jadi → Penjualan)")
}

@Entity(tableName = "business_profile")
data class BusinessProfile(
  @PrimaryKey val id: Int = 1,
  val name: String = "Bisnis Saya",
  val type: String = BusinessType.JUAL_BELI.name,
  val phone: String = "",
  val address: String = "",
  val logoUri: String = "",
  val themeSetting: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
  val isConfigured: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)
