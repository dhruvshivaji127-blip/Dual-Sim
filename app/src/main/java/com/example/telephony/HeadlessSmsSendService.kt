package com.example.telephony

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * Required component for Google Play Default SMS App compliance.
 * Handles headless "respond via message" intents (e.g. declining calls with quick SMS).
 */
class HeadlessSmsSendService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
