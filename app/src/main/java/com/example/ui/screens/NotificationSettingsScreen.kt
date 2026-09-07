package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MessageEntity
import com.example.notifications.SmsNotificationManager
import com.example.ui.SmsViewModel
import com.example.ui.components.SimSlotBadge
import com.example.ui.theme.Sim1Container
import com.example.ui.theme.Sim1Primary
import com.example.ui.theme.Sim2Container
import com.example.ui.theme.Sim2Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    viewModel: SmsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val simProfiles by viewModel.simProfiles.collectAsState()

    val sim1 = simProfiles.firstOrNull { it.slotIndex == 0 }
    val sim2 = simProfiles.firstOrNull { it.slotIndex == 1 }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Notification Management", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Per-SIM Notification Channels",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Configure alert preferences, vibration, and quiet hours independently for each SIM.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // SIM 1 Settings
            item {
                if (sim1 != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SimSlotBadge(slotIndex = 0, customName = sim1.customName)
                                }
                                Switch(
                                    checked = sim1.notificationsEnabled,
                                    onCheckedChange = {
                                        viewModel.updateSimProfile(sim1.copy(notificationsEnabled = it))
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Carrier: ${sim1.carrierName}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Mute Promotional SMS", fontSize = 13.sp)
                                Switch(
                                    checked = sim1.mutePromotions,
                                    onCheckedChange = {
                                        viewModel.updateSimProfile(sim1.copy(mutePromotions = it))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // SIM 2 Settings (with Quiet Hours for Work)
            item {
                if (sim2 != null) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SimSlotBadge(slotIndex = 1, customName = sim2.customName)
                                }
                                Switch(
                                    checked = sim2.notificationsEnabled,
                                    onCheckedChange = {
                                        viewModel.updateSimProfile(sim2.copy(notificationsEnabled = it))
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Carrier: ${sim2.carrierName}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Quiet Hours Feature
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = null,
                                        tint = Sim2Primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text("Work Quiet Hours", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Silence work messages outside office hours", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                                Switch(
                                    checked = sim2.quietHoursEnabled,
                                    onCheckedChange = {
                                        viewModel.updateSimProfile(sim2.copy(quietHoursEnabled = it))
                                    }
                                )
                            }

                            AnimatedVisibility(visible = sim2.quietHoursEnabled) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Text(
                                        text = "Quiet Window: ${sim2.quietHoursStartHour}:00 (Evening) to 0${sim2.quietHoursEndHour}:00 (Morning)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Sim2Primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Messages received during this window will be saved in your SIM 2 section without sound or vibration.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Notification Management
            item {
                Text(
                    text = "Category Notification Behaviors",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CategoryNotificationRow(
                            title = "High Priority Verification OTPs",
                            description = "Always sound and display quick 'Copy OTP' action button",
                            enabled = true
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        CategoryNotificationRow(
                            title = "Banking & Account Alerts",
                            description = "Standard alert sound for financial debits and transactions",
                            enabled = true
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        CategoryNotificationRow(
                            title = "Promotional Offers & Deals",
                            description = "Deliver silently or auto-mute to minimize distraction",
                            enabled = false
                        )
                    }
                }
            }

            // Test Notification Trigger
            item {
                OutlinedButton(
                    onClick = {
                        val testMsg = MessageEntity(
                            id = System.currentTimeMillis() % 10000,
                            senderOrRecipient = "AUTH-PORTAL",
                            contactName = "Security Gateway",
                            body = "Your Dual SIM login verification code is 849201. Do not disclose.",
                            timestamp = System.currentTimeMillis(),
                            isIncoming = true,
                            simSlotIndex = 0,
                            category = MessageEntity.CATEGORY_OTP,
                            otpCode = "849201"
                        )
                        SmsNotificationManager.notifyIncomingSms(context, testMsg, sim1)
                        Toast.makeText(context, "Test notification triggered!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trigger Test Notification with OTP Copy Button")
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CategoryNotificationRow(
    title: String,
    description: String,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            imageVector = if (enabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
            contentDescription = null,
            tint = if (enabled) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }
}
