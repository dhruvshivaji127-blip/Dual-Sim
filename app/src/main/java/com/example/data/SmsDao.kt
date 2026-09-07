package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE simSlotIndex = :slotIndex ORDER BY timestamp DESC")
    fun getMessagesForSimFlow(slotIndex: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE senderOrRecipient = :senderOrRecipient ORDER BY timestamp ASC")
    fun getMessagesForConversation(senderOrRecipient: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE senderOrRecipient = :senderOrRecipient")
    suspend fun deleteConversation(senderOrRecipient: String)

    @Query("UPDATE messages SET isRead = 1 WHERE senderOrRecipient = :senderOrRecipient")
    suspend fun markConversationAsRead(senderOrRecipient: String)

    @Query("UPDATE messages SET isRead = 1 WHERE id = :id")
    suspend fun markMessageAsRead(id: Long)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()

    // Filter Rules
    @Query("SELECT * FROM filter_rules ORDER BY id ASC")
    fun getFilterRulesFlow(): Flow<List<FilterRuleEntity>>

    @Query("SELECT * FROM filter_rules WHERE isEnabled = 1")
    suspend fun getActiveFilterRulesSync(): List<FilterRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilterRule(rule: FilterRuleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilterRules(rules: List<FilterRuleEntity>)

    @Update
    suspend fun updateFilterRule(rule: FilterRuleEntity)

    @Query("DELETE FROM filter_rules WHERE id = :id")
    suspend fun deleteFilterRule(id: Long)

    // SIM Profiles
    @Query("SELECT * FROM sim_profiles ORDER BY slotIndex ASC")
    fun getSimProfilesFlow(): Flow<List<SimProfileEntity>>

    @Query("SELECT * FROM sim_profiles ORDER BY slotIndex ASC")
    suspend fun getSimProfilesSync(): List<SimProfileEntity>

    @Query("SELECT * FROM sim_profiles WHERE slotIndex = :slotIndex LIMIT 1")
    suspend fun getSimProfile(slotIndex: Int): SimProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimProfiles(profiles: List<SimProfileEntity>)

    @Update
    suspend fun updateSimProfile(profile: SimProfileEntity)
}
