package com.example.telephony

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import androidx.core.content.ContextCompat

data class SubscriptionSlotInfo(
    val slotIndex: Int, // 0 or 1
    val subscriptionId: Int,
    val displayName: String,
    val carrierName: String,
    val countryIso: String
)

object DualSimManager {

    fun getActiveSubscriptions(context: Context): List<SubscriptionSlotInfo> {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            return emptyList()
        }

        return try {
            val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
            val activeList: List<SubscriptionInfo>? = subscriptionManager?.activeSubscriptionInfoList

            activeList?.map { info ->
                SubscriptionSlotInfo(
                    slotIndex = info.simSlotIndex,
                    subscriptionId = info.subscriptionId,
                    displayName = info.displayName?.toString() ?: "SIM ${info.simSlotIndex + 1}",
                    carrierName = info.carrierName?.toString() ?: "Carrier ${info.simSlotIndex + 1}",
                    countryIso = info.countryIso ?: ""
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getSmsManagerForSubscription(context: Context, subscriptionId: Int): SmsManager {
        return if (subscriptionId >= 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val baseManager = context.getSystemService(SmsManager::class.java)
                baseManager.createForSubscriptionId(subscriptionId)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        }
    }

    fun sendSms(
        context: Context,
        subscriptionId: Int,
        recipient: String,
        message: String,
        sentIntent: PendingIntent? = null,
        deliveryIntent: PendingIntent? = null
    ): Boolean {
        return try {
            val smsManager = getSmsManagerForSubscription(context, subscriptionId)
            val parts = smsManager.divideMessage(message)

            if (parts.size > 1) {
                val sentIntents = ArrayList<PendingIntent?>()
                val deliveryIntents = ArrayList<PendingIntent?>()
                for (i in parts.indices) {
                    sentIntents.add(sentIntent)
                    deliveryIntents.add(deliveryIntent)
                }
                smsManager.sendMultipartTextMessage(recipient, null, parts, sentIntents, deliveryIntents)
            } else {
                smsManager.sendTextMessage(recipient, null, message, sentIntent, deliveryIntent)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
