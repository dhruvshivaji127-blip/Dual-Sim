package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ConversationSummary
import com.example.data.MessageEntity
import com.example.ui.AppScreen
import com.example.ui.SmsViewModel
import com.example.ui.components.CategoryBadge
import com.example.ui.components.DefaultAppBanner
import com.example.ui.components.NewMessageDialog
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
fun ConversationListScreen(
    viewModel: SmsViewModel,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val conversations by viewModel.conversations.collectAsState()
    val simTab by viewModel.simTab.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isDefaultSmsApp by viewModel.isDefaultSmsApp.collectAsState()
    val hasPermissions by viewModel.hasSmsPermissions.collectAsState()
    val simProfiles by viewModel.simProfiles.collectAsState()

    var showNewMessageDialog by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val sim1 = simProfiles.firstOrNull { it.slotIndex == 0 }
    val sim2 = simProfiles.firstOrNull { it.slotIndex == 1 }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Dual SIM SMS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 19.sp
                            )
                            Text(
                                text = "${sim1?.customName ?: "SIM 1"} & ${sim2?.customName ?: "SIM 2"}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {
                        IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                            Icon(Icons.Default.Search, contentDescription = "Search messages")
                        }
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.FILTER_RULES) }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Smart Filters")
                        }
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.NOTIFICATIONS) }) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notification Settings")
                        }
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.SIM_SETTINGS) }) {
                            Icon(Icons.Default.Settings, contentDescription = "SIM Card Settings")
                        }
                    }
                )

                // Search Bar Expandable
                AnimatedVisibility(visible = isSearchExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search by sender, number or keyword...") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Dual SIM Tab Switcher (Unified vs SIM 1 vs SIM 2)
                val tabTitles = listOf(
                    "All SIMs",
                    sim1?.customName ?: "SIM 1 (Personal)",
                    sim2?.customName ?: "SIM 2 (Work)"
                )
                val selectedTabIndex = when (simTab) {
                    0 -> 1
                    1 -> 2
                    else -> 0
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        val indicatorColor = when (selectedTabIndex) {
                            1 -> Sim1Primary
                            2 -> Sim2Primary
                            else -> MaterialTheme.colorScheme.primary
                        }
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = indicatorColor
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        val activeColor = when (index) {
                            1 -> Sim1Primary
                            2 -> Sim2Primary
                            else -> MaterialTheme.colorScheme.primary
                        }
                        Tab(
                            selected = isSelected,
                            onClick = {
                                val slot = when (index) {
                                    1 -> 0
                                    2 -> 1
                                    else -> -1
                                }
                                viewModel.setSimTab(slot)
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (index > 0) {
                                        Icon(
                                            imageVector = Icons.Default.SimCard,
                                            contentDescription = null,
                                            tint = if (isSelected) activeColor else Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        )
                    }
                }

                // Category Filter Pills
                val categories = listOf(
                    MessageEntity.CATEGORY_ALL to "All",
                    MessageEntity.CATEGORY_PERSONAL to "Personal",
                    MessageEntity.CATEGORY_OTP to "OTP / Codes",
                    MessageEntity.CATEGORY_TRANSACTIONS to "Banking",
                    MessageEntity.CATEGORY_UPDATES to "Updates",
                    MessageEntity.CATEGORY_PROMOTIONS to "Promotions",
                    MessageEntity.CATEGORY_SPAM to "Spam"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { (catKey, catLabel) ->
                        val isCatSelected = categoryFilter.equals(catKey, ignoreCase = true)
                        val icon = when (catKey) {
                            MessageEntity.CATEGORY_OTP -> Icons.Default.Security
                            MessageEntity.CATEGORY_TRANSACTIONS -> Icons.Default.AccountBalance
                            MessageEntity.CATEGORY_PROMOTIONS -> Icons.Default.LocalOffer
                            MessageEntity.CATEGORY_SPAM -> Icons.Default.Warning
                            else -> null
                        }

                        InputChip(
                            selected = isCatSelected,
                            onClick = { viewModel.setCategoryFilter(catKey) },
                            label = { Text(catLabel, fontSize = 12.sp) },
                            leadingIcon = if (icon != null) {
                                { Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewMessageDialog = true },
                containerColor = if (simTab == 1) Sim2Primary else Sim1Primary,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Message")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Compose", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Default App / Permission Warning Banner
            DefaultAppBanner(
                isDefaultSmsApp = isDefaultSmsApp,
                hasPermissions = hasPermissions,
                onRequestDefault = {
                    (context as? Activity)?.let { viewModel.requestDefaultSmsApp(it) }
                },
                onRequestPermissions = onRequestPermissions
            )

            if (conversations.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SimCard,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Messages Found",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (simTab >= 0) {
                                "No conversations found in ${if (simTab == 1) (sim2?.customName ?: "SIM 2") else (sim1?.customName ?: "SIM 1")} section."
                            } else {
                                "Your inbox is empty. Try generating demo messages or start a new conversation."
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = { viewModel.populateDemoDualSimData() }
                        ) {
                            Icon(Icons.Default.SimCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Populate Demo Dual SIM Chats")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversations, key = { it.senderOrRecipient }) { summary ->
                        ConversationCard(
                            summary = summary,
                            simProfiles = simProfiles,
                            onClick = { viewModel.openConversation(summary.senderOrRecipient) },
                            onDelete = { viewModel.deleteConversation(summary.senderOrRecipient) },
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
    }

    if (showNewMessageDialog) {
        NewMessageDialog(
            simProfiles = simProfiles,
            onDismiss = { showNewMessageDialog = false },
            onSend = { recipient, text, simSlot ->
                viewModel.sendMessage(recipient, text, simSlot)
                viewModel.openConversation(recipient)
            }
        )
    }
}

@Composable
fun ConversationCard(
    summary: ConversationSummary,
    simProfiles: List<com.example.data.SimProfileEntity>,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onCopyOtp: (String) -> Unit
) {
    val simProfile = simProfiles.firstOrNull { it.slotIndex == summary.simSlotIndex }
    val isUnread = summary.unreadCount > 0
    val isSim2 = summary.simSlotIndex == 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                val avatarBg = if (isSim2) Sim2Container else Sim1Container
                val avatarTint = if (isSim2) Sim2Primary else Sim1Primary
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (summary.contactName?.take(1) ?: summary.senderOrRecipient.take(1)).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = avatarTint,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = summary.contactName ?: summary.senderOrRecipient,
                            fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = formatTimestamp(summary.lastTimestamp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // SIM Card Indicator Badge
                        SimSlotBadge(
                            slotIndex = summary.simSlotIndex,
                            customName = simProfile?.customName,
                            compact = true
                        )

                        // Category Badge
                        if (summary.category != MessageEntity.CATEGORY_PERSONAL) {
                            CategoryBadge(category = summary.category)
                        }

                        if (summary.isMuted) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Muted",
                                tint = Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Last message body preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.lastMessage,
                    fontSize = 13.sp,
                    color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (isUnread) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${summary.unreadCount}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Quick OTP Copy action if detected
            if (!summary.otpCode.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(OtpGreenContainer)
                        .clickable { onCopyOtp(summary.otpCode) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = OtpGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Copy Code: ${summary.otpCode}",
                        color = OtpGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val oneMinute = 60 * 1000L
    val oneHour = 60 * oneMinute
    val oneDay = 24 * oneHour

    return when {
        diff < oneMinute -> "Just now"
        diff < oneHour -> "${diff / oneMinute}m ago"
        diff < oneDay -> "${diff / oneHour}h ago"
        diff < 7 * oneDay -> SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
