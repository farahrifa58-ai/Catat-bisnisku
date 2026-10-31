package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "production_records")
data class ProductionRecord(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val outputItemId: Long,
  val outputItemName: String,
  val outputQty: Double,
  val outputUnit: String,
  val totalMaterialCost: Double,
  val unitCost: Double,
  val notes: String = "",
  val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "production_materials")
data class ProductionMaterialUsage(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val productionRecordId: Long,
  val rawItemId: Long,
  val rawItemName: String,
  val qtyUsed: Double,
  val unit: String,
  val costPerUnit: Double,
  val totalCost: Double
)
