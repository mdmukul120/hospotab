package com.example.data.repository

import com.example.data.db.HotspotProfileDao
import com.example.data.model.HotspotProfile
import kotlinx.coroutines.flow.Flow

class HotspotRepository(private val dao: HotspotProfileDao) {
    val allProfiles: Flow<List<HotspotProfile>> = dao.getAllProfiles()

    suspend fun insertProfile(profile: HotspotProfile): Long {
        return dao.insertProfile(profile)
    }

    suspend fun updateProfile(profile: HotspotProfile) {
        dao.updateProfile(profile)
    }

    suspend fun deleteProfile(profile: HotspotProfile) {
        dao.deleteProfile(profile)
    }

    suspend fun getProfileById(id: Int): HotspotProfile? {
        return dao.getProfileById(id)
    }
}
