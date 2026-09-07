package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index("senderOrRecipient"),
        Index("timestamp"),
        Index("simSlotIndex"),
        Index("category")
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val senderOrRecipient: String,
    val contactName: String? = null,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isIncoming: Boolean = true,
    val simSlotIndex: Int = 0, // 0 = SIM 1, 1 = SIM 2, -1 = unknown
    val subId: Int = -1,
    val category: String = CATEGORY_PERSONAL,
    val isRead: Boolean = false,
    val isSpam: Boolean = false,
    val isMuted: Boolean = false,
    val deliveryStatus: String = STATUS_DELIVERED,
    val otpCode: String? = null
) {
    companion object {
        const val CATEGORY_ALL = "ALL"
        const val CATEGORY_PERSONAL = "PERSONAL"
        const val CATEGORY_OTP = "OTP"
        const val CATEGORY_TRANSACTIONS = "TRANSACTIONS"
        const val CATEGORY_UPDATES = "UPDATES"
        const val CATEGORY_PROMOTIONS = "PROMOTIONS"
        const val CATEGORY_SPAM = "SPAM"

        const val STATUS_SENDING = "SENDING"
        const val STATUS_SENT = "SENT"
        const val STATUS_DELIVERED = "DELIVERED"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_RECEIVED = "RECEIVED"
    }
}
