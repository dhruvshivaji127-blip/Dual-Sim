package com.example.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.example.data.AppDatabase
import com.example.data.MessageEntity
import com.example.filtering.SmsFilterEngine
import com.example.notifications.SmsNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Telephony.Sms.Intents.SMS_DELIVER_ACTION &&
            action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION
        ) {
            return
        }

        val messages: Array<SmsMessage>? = try {
            Telephony.Sms.Intents.getMessagesFromIntent(intent)
        } catch (e: Exception) {
            null
        }

        if (messages.isNullOrEmpty()) return

        // Extract Dual SIM slot / subscription ID from intent extras
        val subId = intent.getIntExtra("subscription",
            intent.getIntExtra("sub_id",
                intent.getIntExtra("subscription_id", -1)
            )
        )
        var slotIndex = intent.getIntExtra("slot",
            intent.getIntExtra("simSlot",
                intent.getIntExtra("simId", -1)
            )
        )

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val dao = db.smsDao()
                val activeRules = dao.getActiveFilterRulesSync()

                // If slotIndex is unknown (-1), try to map from subId
                if (slotIndex < 0 && subId >= 0) {
                    val activeSubs = DualSimManager.getActiveSubscriptions(context)
                    val matchedSub = activeSubs.firstOrNull { it.subscriptionId == subId }
                    if (matchedSub != null) {
                        slotIndex = matchedSub.slotIndex
                    }
                }
                if (slotIndex < 0) {
                    slotIndex = 0 // Default to SIM 1
                }

                // Group messages by originating sender
                val groupedBySender = messages.groupBy { it.displayOriginatingAddress ?: "Unknown" }

                for ((sender, parts) in groupedBySender) {
                    val fullBody = parts.joinToString("") { it.displayMessageBody ?: "" }
                    val timestamp = parts.firstOrNull()?.timestampMillis ?: System.currentTimeMillis()

                    // Apply automated filtering
                    val filterResult = SmsFilterEngine.evaluate(
                        sender = sender,
                        body = fullBody,
                        currentSimSlot = slotIndex,
                        activeRules = activeRules
                    )

                    val resolvedSlot = filterResult.assignedSimSlot
                    val messageEntity = MessageEntity(
                        senderOrRecipient = sender,
                        contactName = null,
                        body = fullBody,
                        timestamp = timestamp,
                        isIncoming = true,
                        simSlotIndex = resolvedSlot,
                        subId = subId,
                        category = filterResult.category,
                        isRead = false,
                        isSpam = filterResult.isSpam,
                        isMuted = filterResult.isMuted,
                        deliveryStatus = MessageEntity.STATUS_RECEIVED,
                        otpCode = filterResult.extractedOtp
                    )

                    val insertedId = dao.insertMessage(messageEntity)
                    val savedMessage = messageEntity.copy(id = insertedId)

                    // Fetch SIM profile for notification preferences
                    val simProfile = dao.getSimProfile(resolvedSlot)

                    // Trigger notification management
                    SmsNotificationManager.notifyIncomingSms(context, savedMessage, simProfile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
