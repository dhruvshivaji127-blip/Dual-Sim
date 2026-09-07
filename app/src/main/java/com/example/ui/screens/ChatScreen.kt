package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.SimProfileEntity
import com.example.ui.SmsViewModel
import com.example.ui.components.CategoryBadge
import com.example.ui.components.SimSlotBadge
import com.example.ui.theme.OtpGreen
import com.example.ui.theme.OtpGreenContainer
import com.example.ui.theme.Sim1Container
import com.example.ui.theme.Sim1OnContainer
import com.example.ui.theme.Sim1Primary
import com.example.ui.theme.Sim2Container
import com.example.ui.theme.Sim2OnContainer
import com.example.ui.theme.Sim2Primary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: SmsViewModel,
    sender: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.activeConversationMessages.collectAsState()
    val simProfiles by viewModel.simProfiles.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var selectedSendingSimSlot by remember { mutableIntStateOf(0) }

    val sim1 = simProfiles.firstOrNull { it.slotIndex == 0 }
    val sim2 = simProfiles.firstOrNull { it.slotIndex == 1 }

    val listState = rememberLazyListState()

    // Auto-scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val contactName = messages.firstOrNull { it.contactName != null }?.contactName
                        Text(
                            text = contactName ?: sender,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1
                        )
                        val lastMessage = messages.lastOrNull()
                        val currentSimSlot = lastMessage?.simSlotIndex ?: 0
                        val currentProfile = simProfiles.firstOrNull { it.slotIndex == currentSimSlot }
                        Text(
                            text = "via ${currentProfile?.customName ?: if (currentSimSlot == 1) "SIM 2" else "SIM 1"}",
                            fontSize = 11.sp,
                            color = if (currentSimSlot == 1) Sim2Primary else Sim1Primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteConversation(sender)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Conversation")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Dual SIM Selector Bar before sending
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Send from: ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))

                            // SIM 1 toggle chip
                            val isSim1 = selectedSendingSimSlot == 0
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSim1) Sim1Primary else Sim1Container)
                                    .clickable { selectedSendingSimSlot = 0 }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SimCard,
                                        contentDescription = null,
                                        tint = if (isSim1) Color.White else Sim1OnContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = sim1?.customName ?: "SIM 1 (Personal)",
                                        color = if (isSim1) Color.White else Sim1OnContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // SIM 2 toggle chip
                            val isSim2 = selectedSendingSimSlot == 1
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSim2) Sim2Primary else Sim2Container)
                                    .clickable { selectedSendingSimSlot = 1 }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SimCard,
                                        contentDescription = null,
                                        tint = if (isSim2) Color.White else Sim2OnContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = sim2?.customName ?: "SIM 2 (Work)",
                                        color = if (isSim2) Color.White else Sim2OnContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Char counter
                        val chars = messageText.length
                        val parts = if (chars == 0) 1 else ((chars - 1) / 160) + 1
                        Text(
                            text = "$chars/160 ($parts)",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Message Composer Textfield & Send Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = {
                                val activeLabel = if (selectedSendingSimSlot == 1) (sim2?.customName ?: "SIM 2") else (sim1?.customName ?: "SIM 1")
                                Text("Type via $activeLabel...", fontSize = 14.sp)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (selectedSendingSimSlot == 1) Sim2Primary else Sim1Primary
                            ),
                            maxLines = 4
                        )

                        val canSend = messageText.isNotBlank()
                        val sendBg = if (selectedSendingSimSlot == 1) Sim2Primary else Sim1Primary

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (canSend) sendBg else Color.LightGray)
                                .clickable(enabled = canSend) {
                                    viewModel.sendMessage(sender, messageText.trim(), selectedSendingSimSlot)
                                    messageText = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    simProfiles = simProfiles,
                    onCopyOtp = { otp ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("OTP Code", otp)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "OTP $otp copied!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: MessageEntity,
    simProfiles: List<SimProfileEntity>,
    onCopyOtp: (String) -> Unit
) {
    val isOutgoing = !message.isIncoming
    val simProfile = simProfiles.firstOrNull { it.slotIndex == message.simSlotIndex }
    val isSim2 = message.simSlotIndex == 1

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
    ) {
        // Bubble container
        val bubbleColor = if (isOutgoing) {
            if (isSim2) Sim2Container else Sim1Container
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }

        val bubbleTextColor = if (isOutgoing) {
            if (isSim2) Sim2OnContainer else Sim1OnContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

        val bubbleShape = if (isOutgoing) {
            RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        } else {
            RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        }

        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            tonalElevation = 1.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header inside bubble: SIM Badge & Category
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SimSlotBadge(
                        slotIndex = message.simSlotIndex,
                        customName = simProfile?.customName,
                        compact = true
                    )
                    if (message.category != MessageEntity.CATEGORY_PERSONAL) {
                        CategoryBadge(category = message.category)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Message Text
                Text(
                    text = message.body,
                    color = bubbleTextColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Dedicated OTP quick card if available
                if (!message.otpCode.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = OtpGreenContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCopyOtp(message.otpCode) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "VERIFICATION CODE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OtpGreen
                                )
                                Text(
                                    text = message.otpCode,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OtpGreen,
                                    letterSpacing = 2.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy OTP",
                                    tint = OtpGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Copy",
                                    color = OtpGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp & status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    if (isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (message.deliveryStatus) {
                                MessageEntity.STATUS_DELIVERED -> "✓✓"
                                MessageEntity.STATUS_SENT -> "✓"
                                else -> "•"
                            },
                            fontSize = 10.sp,
                            color = if (isSim2) Sim2Primary else Sim1Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.widthIn(max: androidx.compose.ui.unit.Dp): Modifier =
    this.then(Modifier.padding(end = 0.dp))
