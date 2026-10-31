package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val amount: Double,
  val category: String,
  val date: Long = System.currentTimeMillis(),
  val notes: String = ""
)

val DEFAULT_EXPENSE_CATEGORIES = listOf(
  "Transportasi",
  "Listrik & Air",
  "Gas & Bahan Bakar",
  "Gaji Karyawan",
  "Sewa Tempat",
  "Kemasan & Plastik",
  "Peralatan & Perbaikan",
  "Operasional Lain"
)
