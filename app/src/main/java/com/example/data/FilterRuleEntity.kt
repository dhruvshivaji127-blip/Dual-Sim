package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filter_rules")
data class FilterRuleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ruleName: String,
    val pattern: String, // Keyword or regex
    val matchField: String = FIELD_BODY, // "BODY" or "SENDER"
    val isRegex: Boolean = false,
    val targetCategory: String = MessageEntity.CATEGORY_PERSONAL,
    val autoMute: Boolean = false,
    val moveToSpam: Boolean = false,
    val assignSimSlot: Int = -1, // -1 means no assignment, 0 = SIM 1, 1 = SIM 2
    val isEnabled: Boolean = true,
    val isSystemDefault: Boolean = false
) {
    companion object {
        const val FIELD_BODY = "BODY"
        const val FIELD_SENDER = "SENDER"
    }
}
