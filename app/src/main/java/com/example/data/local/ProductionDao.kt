package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ProductionMaterialUsage
import com.example.data.model.ProductionRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductionDao {
  @Query("SELECT * FROM production_records ORDER BY date DESC")
  fun getAllRecords(): Flow<List<ProductionRecord>>

  @Query("SELECT * FROM production_materials WHERE productionRecordId = :recordId")
  fun getMaterialsForRecord(recordId: Long): Flow<List<ProductionMaterialUsage>>

  @Query("SELECT * FROM production_materials")
  suspend fun getAllMaterialsSync(): List<ProductionMaterialUsage>

  @Query("SELECT * FROM production_records")
  suspend fun getAllRecordsSync(): List<ProductionRecord>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecord(record: ProductionRecord): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMaterials(materials: List<ProductionMaterialUsage>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllRecords(records: List<ProductionRecord>)

  @Query("DELETE FROM production_records")
  suspend fun deleteAllRecords()

  @Query("DELETE FROM production_materials")
  suspend fun deleteAllMaterials()
}
