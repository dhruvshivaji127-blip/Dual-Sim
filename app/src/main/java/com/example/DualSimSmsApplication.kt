package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.notifications.SmsNotificationManager

class DualSimSmsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Notification Channels for SIM 1, SIM 2, OTP, and Transactions
        SmsNotificationManager.createChannels(this)
        // Ensure Database is initialized
        AppDatabase.getDatabase(this)
    }
}
