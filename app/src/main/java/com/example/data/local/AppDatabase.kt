package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.Expense
import com.example.data.model.Item
import com.example.data.model.ProductionMaterialUsage
import com.example.data.model.ProductionRecord
import com.example.data.model.Sale
import com.example.data.model.StockMovement

@Database(
  entities = [
    BusinessProfile::class,
    Item::class,
    StockMovement::class,
    ProductionRecord::class,
    ProductionMaterialUsage::class,
    Sale::class,
    Expense::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun businessDao(): BusinessDao
  abstract fun itemDao(): ItemDao
  abstract fun stockMovementDao(): StockMovementDao
  abstract fun productionDao(): ProductionDao
  abstract fun saleDao(): SaleDao
  abstract fun expenseDao(): ExpenseDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "catat_bisnisku_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
