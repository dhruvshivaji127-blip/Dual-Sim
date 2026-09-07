package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sim_profiles")
data class SimProfileEntity(
    @PrimaryKey
    val slotIndex: Int, // 0 = SIM 1, 1 = SIM 2
    val subId: Int = -1,
    val customName: String,
    val carrierName: String = "Carrier $slotIndex",
    val phoneNumber: String = "",
    val colorHex: Long, // e.g. 0xFF1E88E5 or 0xFF7C4DFF
    val notificationsEnabled: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartHour: Int = 18, // 6 PM
    val quietHoursEndHour: Int = 9,    // 9 AM
    val mutePromotions: Boolean = true,
    val isDefaultSending: Boolean = (slotIndex == 0)
)
