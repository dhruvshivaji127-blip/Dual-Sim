package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.MessageEntity
import com.example.data.SimProfileEntity
import com.example.telephony.SmsActionReceiver
import java.util.Calendar

object SmsNotificationManager {

    const val CHANNEL_SIM_1 = "channel_sim_0"
    const val CHANNEL_SIM_2 = "channel_sim_1"
    const val CHANNEL_OTP = "channel_otp"
    const val CHANNEL_TRANSACTIONS = "channel_transactions"
    const val CHANNEL_PROMOTIONS = "channel_promotions"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return

            val sim1Channel = NotificationChannel(
                CHANNEL_SIM_1,
                "SIM 1 (Personal) Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming SMS notifications for SIM 1"
                enableVibration(true)
            }

            val sim2Channel = NotificationChannel(
                CHANNEL_SIM_2,
                "SIM 2 (Work) Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming SMS notifications for SIM 2"
                enableVibration(true)
            }

            val otpChannel = NotificationChannel(
                CHANNEL_OTP,
                "Verification Codes & OTP",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical verification codes and one-time passwords"
                enableVibration(true)
            }

            val txnChannel = NotificationChannel(
                CHANNEL_TRANSACTIONS,
                "Banking & Transactions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Bank alerts, debits, and credits"
            }

            val promoChannel = NotificationChannel(
                CHANNEL_PROMOTIONS,
                "Promotions & Offers",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Discounts, deals, and promotional alerts"
            }

            notificationManager.createNotificationChannels(
                listOf(sim1Channel, sim2Channel, otpChannel, txnChannel, promoChannel)
            )
        }
    }

    fun isWithinQuietHours(profile: SimProfileEntity): Boolean {
        if (!profile.quietHoursEnabled) return false

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val start = profile.quietHoursStartHour
        val end = profile.quietHoursEndHour

        return if (start < end) {
            currentHour in start until end
        } else {
            // Over midnight (e.g. 18:00 to 09:00)
            currentHour >= start || currentHour < end
        }
    }

    fun notifyIncomingSms(
        context: Context,
        message: MessageEntity,
        simProfile: SimProfileEntity?
    ) {
        // Notification permission check for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        // Check if notifications are disabled for this SIM
        if (simProfile != null && !simProfile.notificationsEnabled) {
            return
        }

        // Check if muted by rule
        if (message.isMuted) {
            return
        }

        // Check if promotions muted
        if (message.category == MessageEntity.CATEGORY_PROMOTIONS && simProfile?.mutePromotions == true) {
            return
        }

        // Check quiet hours
        val inQuietHours = simProfile != null && isWithinQuietHours(simProfile)

        // Select appropriate channel
        val channelId = when {
            message.category == MessageEntity.CATEGORY_OTP -> CHANNEL_OTP
            message.category == MessageEntity.CATEGORY_TRANSACTIONS -> CHANNEL_TRANSACTIONS
            message.category == MessageEntity.CATEGORY_PROMOTIONS -> CHANNEL_PROMOTIONS
            message.simSlotIndex == 1 -> CHANNEL_SIM_2
            else -> CHANNEL_SIM_1
        }

        // Main Tap Intent -> Open MainActivity with conversation
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("conversation_sender", message.senderOrRecipient)
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            message.id.toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val simLabel = simProfile?.customName ?: if (message.simSlotIndex == 1) "SIM 2" else "SIM 1"
        val title = "${message.contactName ?: message.senderOrRecipient} • [$simLabel]"

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .setPriority(
                if (inQuietHours) NotificationCompat.PRIORITY_LOW
                else if (message.category == MessageEntity.CATEGORY_OTP) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )

        if (inQuietHours) {
            builder.setSilent(true)
        }

        // Add Quick Action: Copy OTP if detected
        if (!message.otpCode.isNullOrBlank()) {
            val copyIntent = Intent(context, SmsActionReceiver::class.java).apply {
                action = SmsActionReceiver.ACTION_COPY_OTP
                putExtra(SmsActionReceiver.EXTRA_OTP_CODE, message.otpCode)
                putExtra(SmsActionReceiver.EXTRA_MESSAGE_ID, message.id)
            }
            val copyPendingIntent = PendingIntent.getBroadcast(
                context,
                (message.id * 10 + 1).toInt(),
                copyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                0,
                "Copy OTP: ${message.otpCode}",
                copyPendingIntent
            )
        }

        // Add Quick Action: Mark as Read
        val readIntent = Intent(context, SmsActionReceiver::class.java).apply {
            action = SmsActionReceiver.ACTION_MARK_READ
            putExtra(SmsActionReceiver.EXTRA_SENDER, message.senderOrRecipient)
            putExtra(SmsActionReceiver.EXTRA_MESSAGE_ID, message.id)
        }
        val readPendingIntent = PendingIntent.getBroadcast(
            context,
            (message.id * 10 + 2).toInt(),
            readIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            0,
            "Mark as Read",
            readPendingIntent
        )

        val notificationId = (message.senderOrRecipient.hashCode() + message.id).toInt()
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}
