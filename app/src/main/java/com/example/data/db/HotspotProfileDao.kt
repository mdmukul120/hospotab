package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.HotspotProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface HotspotProfileDao {
    @Query("SELECT * FROM hotspot_profiles ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<HotspotProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: HotspotProfile): Long

    @Update
    suspend fun updateProfile(profile: HotspotProfile)

    @Delete
    suspend fun deleteProfile(profile: HotspotProfile)

    @Query("SELECT * FROM hotspot_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Int): HotspotProfile?
}
