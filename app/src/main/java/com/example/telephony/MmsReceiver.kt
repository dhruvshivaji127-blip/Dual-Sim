package com.example.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Required component for Google Play Default SMS App compliance.
 * Handles incoming MMS push notifications.
 */
class MmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // MMS push notifications stub for Default SMS app requirement
    }
}
