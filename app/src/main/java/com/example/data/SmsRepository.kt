package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SmsRepository(private val dao: SmsDao) {

    val allMessagesFlow: Flow<List<MessageEntity>> = dao.getAllMessagesFlow()
    val filterRulesFlow: Flow<List<FilterRuleEntity>> = dao.getFilterRulesFlow()
    val simProfilesFlow: Flow<List<SimProfileEntity>> = dao.getSimProfilesFlow()

    fun getConversationSummaries(
        simSlotFilter: Int = -1, // -1 = All, 0 = SIM 1, 1 = SIM 2
        categoryFilter: String = MessageEntity.CATEGORY_ALL,
        searchQuery: String = ""
    ): Flow<List<ConversationSummary>> {
        return allMessagesFlow.map { messages ->
            // Filter by SIM slot if specified
            val filteredBySim = if (simSlotFilter >= 0) {
                messages.filter { it.simSlotIndex == simSlotFilter }
            } else {
                messages
            }

            // Group by sender / recipient
            val grouped = filteredBySim.groupBy { it.senderOrRecipient }

            grouped.mapNotNull { (sender, threadMessages) ->
                val sorted = threadMessages.sortedByDescending { it.timestamp }
                val latest = sorted.firstOrNull() ?: return@mapNotNull null
                val unreadCount = sorted.count { !it.isRead && it.isIncoming }

                val summary = ConversationSummary(
                    senderOrRecipient = sender,
                    contactName = latest.contactName,
                    lastMessage = latest.body,
                    lastTimestamp = latest.timestamp,
                    unreadCount = unreadCount,
                    simSlotIndex = latest.simSlotIndex,
                    category = latest.category,
                    isMuted = latest.isMuted,
                    isSpam = latest.isSpam,
                    otpCode = sorted.firstOrNull { it.otpCode != null }?.otpCode
                )

                // Category filter check
                val matchesCategory = when (categoryFilter) {
                    MessageEntity.CATEGORY_ALL -> true
                    MessageEntity.CATEGORY_SPAM -> summary.isSpam || summary.category == MessageEntity.CATEGORY_SPAM
                    else -> summary.category.equals(categoryFilter, ignoreCase = true)
                }

                // Search query check
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    summary.senderOrRecipient.contains(searchQuery, ignoreCase = true) ||
                            (summary.contactName?.contains(searchQuery, ignoreCase = true) == true) ||
                            summary.lastMessage.contains(searchQuery, ignoreCase = true)
                }

                if (matchesCategory && matchesSearch) summary else null
            }.sortedByDescending { it.lastTimestamp }
        }
    }

    fun getMessagesForConversation(sender: String): Flow<List<MessageEntity>> {
        return dao.getMessagesForConversation(sender)
    }

    suspend fun insertMessage(message: MessageEntity): Long {
        return dao.insertMessage(message)
    }

    suspend fun insertMessages(messages: List<MessageEntity>) {
        dao.insertMessages(messages)
    }

    suspend fun markConversationAsRead(sender: String) {
        dao.markConversationAsRead(sender)
    }

    suspend fun markMessageAsRead(id: Long) {
        dao.markMessageAsRead(id)
    }

    suspend fun deleteConversation(sender: String) {
        dao.deleteConversation(sender)
    }

    suspend fun deleteMessage(id: Long) {
        dao.deleteMessage(id)
    }

    suspend fun clearAll() {
        dao.clearAllMessages()
    }

    suspend fun getActiveRules(): List<FilterRuleEntity> {
        return dao.getActiveFilterRulesSync()
    }

    suspend fun insertFilterRule(rule: FilterRuleEntity): Long {
        return dao.insertFilterRule(rule)
    }

    suspend fun updateFilterRule(rule: FilterRuleEntity) {
        dao.updateFilterRule(rule)
    }

    suspend fun deleteFilterRule(id: Long) {
        dao.deleteFilterRule(id)
    }

    suspend fun getSimProfilesSync(): List<SimProfileEntity> {
        return dao.getSimProfilesSync()
    }

    suspend fun getSimProfile(slotIndex: Int): SimProfileEntity? {
        return dao.getSimProfile(slotIndex)
    }

    suspend fun updateSimProfile(profile: SimProfileEntity) {
        dao.updateSimProfile(profile)
    }

    suspend fun saveSimProfiles(profiles: List<SimProfileEntity>) {
        dao.insertSimProfiles(profiles)
    }
}
