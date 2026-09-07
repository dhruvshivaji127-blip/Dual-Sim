package com.example.data

data class ConversationSummary(
    val senderOrRecipient: String,
    val contactName: String?,
    val lastMessage: String,
    val lastTimestamp: Long,
    val unreadCount: Int,
    val simSlotIndex: Int,
    val category: String,
    val isMuted: Boolean,
    val isSpam: Boolean,
    val otpCode: String?
)
