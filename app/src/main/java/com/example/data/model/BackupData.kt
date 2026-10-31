package com.example.data.model

data class BackupData(
  val appName: String = "Catat Bisnisku",
  val version: Int = 1,
  val exportDate: Long = System.currentTimeMillis(),
  val businessProfile: BusinessProfile? = null,
  val items: List<Item> = emptyList(),
  val stockMovements: List<StockMovement> = emptyList(),
  val productionRecords: List<ProductionRecord> = emptyList(),
  val productionMaterials: List<ProductionMaterialUsage> = emptyList(),
  val sales: List<Sale> = emptyList(),
  val expenses: List<Expense> = emptyList()
)
