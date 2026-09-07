package com.example.telephony

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COPY_OTP = "com.example.ACTION_COPY_OTP"
        const val ACTION_MARK_READ = "com.example.ACTION_MARK_READ"

        const val EXTRA_OTP_CODE = "extra_otp_code"
        const val EXTRA_MESSAGE_ID = "extra_message_id"
        const val EXTRA_SENDER = "extra_sender"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                when (action) {
                    ACTION_COPY_OTP -> {
                        val otp = intent.getStringExtra(EXTRA_OTP_CODE)
                        if (!otp.isNullOrBlank()) {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("OTP Code", otp)
                            clipboard.setPrimaryClip(clip)

                            launch(Dispatchers.Main) {
                                Toast.makeText(context, "OTP $otp copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    ACTION_MARK_READ -> {
                        val sender = intent.getStringExtra(EXTRA_SENDER)
                        val msgId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)

                        if (!sender.isNullOrBlank()) {
                            db.smsDao().markConversationAsRead(sender)
                        } else if (msgId != -1L) {
                            db.smsDao().markMessageAsRead(msgId)
                        }

                        // Dismiss notification
                        if (msgId != -1L) {
                            val notifId = (sender.hashCode() + msgId).toInt()
                            NotificationManagerCompat.from(context).cancel(notifId)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
