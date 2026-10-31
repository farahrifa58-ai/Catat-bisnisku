package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BusinessProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
  @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
  fun getProfile(): Flow<BusinessProfile?>

  @Query("SELECT * FROM business_profile WHERE id = 1 LIMIT 1")
  suspend fun getProfileSync(): BusinessProfile?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertProfile(profile: BusinessProfile)
}
