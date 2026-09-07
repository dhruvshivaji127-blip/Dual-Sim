package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MessageEntity::class,
        FilterRuleEntity::class,
        SimProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun smsDao(): SmsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dual_sim_sms.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate defaults
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.smsDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: SmsDao) {
            // Default SIM profiles
            val defaultSims = listOf(
                SimProfileEntity(
                    slotIndex = 0,
                    subId = 1,
                    customName = "SIM 1 (Personal)",
                    carrierName = "Carrier 1",
                    phoneNumber = "",
                    colorHex = 0xFF1976D2, // Royal Blue
                    notificationsEnabled = true,
                    quietHoursEnabled = false,
                    quietHoursStartHour = 22,
                    quietHoursEndHour = 7,
                    mutePromotions = true,
                    isDefaultSending = true
                ),
                SimProfileEntity(
                    slotIndex = 1,
                    subId = 2,
                    customName = "SIM 2 (Work)",
                    carrierName = "Carrier 2",
                    phoneNumber = "",
                    colorHex = 0xFF7B1FA2, // Purple / Violet
                    notificationsEnabled = true,
                    quietHoursEnabled = true, // Default work quiet hours
                    quietHoursStartHour = 18, // 6 PM
                    quietHoursEndHour = 9,    // 9 AM
                    mutePromotions = true,
                    isDefaultSending = false
                )
            )
            dao.insertSimProfiles(defaultSims)

            // Default smart filter rules
            val defaultRules = listOf(
                FilterRuleEntity(
                    ruleName = "Security OTP & Verification Codes",
                    pattern = "(?i)\\b(otp|code|verification|passcode|secret|pin)\\b",
                    matchField = FilterRuleEntity.FIELD_BODY,
                    isRegex = true,
                    targetCategory = MessageEntity.CATEGORY_OTP,
                    autoMute = false,
                    moveToSpam = false,
                    assignSimSlot = -1,
                    isEnabled = true,
                    isSystemDefault = true
                ),
                FilterRuleEntity(
                    ruleName = "Banking & Account Transactions",
                    pattern = "(?i)\\b(debited|credited|ac |a/c|balance|atm|txn|vpa|upi|bank|inr|usd|eur)\\b",
                    matchField = FilterRuleEntity.FIELD_BODY,
                    isRegex = true,
                    targetCategory = MessageEntity.CATEGORY_TRANSACTIONS,
                    autoMute = false,
                    moveToSpam = false,
                    assignSimSlot = -1,
                    isEnabled = true,
                    isSystemDefault = true
                ),
                FilterRuleEntity(
                    ruleName = "Promotional Offers & Discounts",
                    pattern = "(?i)\\b(offer|sale|discount|off%|coupon|cashback|promo|voucher|deal)\\b",
                    matchField = FilterRuleEntity.FIELD_BODY,
                    isRegex = true,
                    targetCategory = MessageEntity.CATEGORY_PROMOTIONS,
                    autoMute = true,
                    moveToSpam = false,
                    assignSimSlot = -1,
                    isEnabled = true,
                    isSystemDefault = true
                ),
                FilterRuleEntity(
                    ruleName = "Delivery & Order Tracking Updates",
                    pattern = "(?i)\\b(shipped|out for delivery|arriving|tracking|order id|flight|booking|scheduled)\\b",
                    matchField = FilterRuleEntity.FIELD_BODY,
                    isRegex = true,
                    targetCategory = MessageEntity.CATEGORY_UPDATES,
                    autoMute = false,
                    moveToSpam = false,
                    assignSimSlot = -1,
                    isEnabled = true,
                    isSystemDefault = true
                ),
                FilterRuleEntity(
                    ruleName = "Spam & Phishing Guard",
                    pattern = "(?i)\\b(claim prize|won lottery|click http|verify your card now|urgent account suspended)\\b",
                    matchField = FilterRuleEntity.FIELD_BODY,
                    isRegex = true,
                    targetCategory = MessageEntity.CATEGORY_SPAM,
                    autoMute = true,
                    moveToSpam = true,
                    assignSimSlot = -1,
                    isEnabled = true,
                    isSystemDefault = true
                )
            )
            dao.insertFilterRules(defaultRules)
        }
    }
}
