package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hotspot_profiles")
data class HotspotProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val ssid: String,
    val password: String,
    val securityType: String = "WPA2", // WPA2, WPA3, OPEN
    val isHidden: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
