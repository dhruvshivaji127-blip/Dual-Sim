package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FilterRuleEntity
import com.example.data.MessageEntity
import com.example.ui.SmsViewModel
import com.example.ui.components.CategoryBadge
import com.example.ui.components.SimSlotBadge
import com.example.ui.theme.OtpGreen
import com.example.ui.theme.OtpGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterManagementScreen(
    viewModel: SmsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filterRules by viewModel.filterRules.collectAsState()
    val simProfiles by viewModel.simProfiles.collectAsState()

    var showAddRuleDialog by remember { mutableStateOf(false) }

    // Sandbox state
    var testSender by remember { mutableStateOf("ACME-ALERT") }
    var testBody by remember { mutableStateOf("Your OTP for corporate login is 582910. Valid for 5 mins.") }
    val testResult = remember(testSender, testBody, filterRules) {
        viewModel.testMessageClassification(testSender, testBody, 0)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text("Automated SMS Filters", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRuleDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Rule", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Info banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Automated filters automatically organize incoming SMS into OTP, Banking, Updates, Promotions, or Spam, and apply mute rules.",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {
                // Live Sandbox Testing Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Science, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Live Filter Simulator", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = testSender,
                            onValueChange = { testSender = it },
                            label = { Text("Sender ID", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = testBody,
                            onValueChange = { testBody = it },
                            label = { Text("Message Body", fontSize = 12.sp) },
                            minLines = 2,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Simulation output
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Detected Category: ", fontSize = 12.sp, color = Color.Gray)
                                        CategoryBadge(category = testResult.category)
                                    }
                                    if (testResult.isMuted) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Auto-Muted", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                if (testResult.extractedOtp != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Extracted OTP: ", fontSize = 12.sp, color = Color.Gray)
                                        Text(testResult.extractedOtp, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = OtpGreen)
                                    }
                                }

                                if (testResult.matchedRuleName != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Matched Rule: ${testResult.matchedRuleName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text("Active Filtering Rules (${filterRules.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            items(filterRules, key = { it.id }) { rule ->
                RuleCard(
                    rule = rule,
                    simProfiles = simProfiles,
                    onToggle = { viewModel.toggleFilterRule(rule) },
                    onDelete = { viewModel.deleteFilterRule(rule.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(60.dp)) // FAB spacing
            }
        }
    }

    if (showAddRuleDialog) {
        AddRuleDialog(
            simProfiles = simProfiles,
            onDismiss = { showAddRuleDialog = false },
            onAdd = { name, pattern, field, isRegex, category, autoMute, moveToSpam, assignSim ->
                viewModel.addCustomFilterRule(
                    name = name,
                    pattern = pattern,
                    matchField = field,
                    isRegex = isRegex,
                    targetCategory = category,
                    autoMute = autoMute,
                    moveToSpam = moveToSpam,
                    assignSimSlot = assignSim
                )
                showAddRuleDialog = false
            }
        )
    }
}

@Composable
fun RuleCard(
    rule: FilterRuleEntity,
    simProfiles: List<com.example.data.SimProfileEntity>,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isEnabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rule.ruleName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (rule.isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                    )
                    if (rule.isSystemDefault) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SYSTEM",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Matches ${rule.matchField}: \"${rule.pattern}\"",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryBadge(category = rule.targetCategory)

                    if (rule.autoMute) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Mute", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    if (rule.assignSimSlot in 0..1) {
                        SimSlotBadge(slotIndex = rule.assignSimSlot, compact = true)
                    }
                }
            }

            Switch(
                checked = rule.isEnabled,
                onCheckedChange = { onToggle() }
            )

            if (!rule.isSystemDefault) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleDialog(
    simProfiles: List<com.example.data.SimProfileEntity>,
    onDismiss: () -> Unit,
    onAdd: (
        name: String,
        pattern: String,
        field: String,
        isRegex: Boolean,
        category: String,
        autoMute: Boolean,
        moveToSpam: Boolean,
        assignSim: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf("") }
    var matchField by remember { mutableStateOf(FilterRuleEntity.FIELD_BODY) }
    var isRegex by remember { mutableStateOf(false) }
    var targetCategory by remember { mutableStateOf(MessageEntity.CATEGORY_PERSONAL) }
    var autoMute by remember { mutableStateOf(false) }
    var moveToSpam by remember { mutableStateOf(false) }
    var assignSimSlot by remember { mutableIntStateOf(-1) }

    val categories = listOf(
        MessageEntity.CATEGORY_PERSONAL,
        MessageEntity.CATEGORY_OTP,
        MessageEntity.CATEGORY_TRANSACTIONS,
        MessageEntity.CATEGORY_UPDATES,
        MessageEntity.CATEGORY_PROMOTIONS,
        MessageEntity.CATEGORY_SPAM
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Filter Rule", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Rule Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("Keyword or Phrase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Match field:", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { matchField = FilterRuleEntity.FIELD_BODY }) {
                        Text(
                            "Message Body",
                            fontWeight = if (matchField == FilterRuleEntity.FIELD_BODY) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    TextButton(onClick = { matchField = FilterRuleEntity.FIELD_SENDER }) {
                        Text(
                            "Sender ID",
                            fontWeight = if (matchField == FilterRuleEntity.FIELD_SENDER) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text("Target Category:", fontSize = 12.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        Button(
                            onClick = { targetCategory = cat },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (targetCategory == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (targetCategory == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(cat.take(4), fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        Button(
                            onClick = { targetCategory = cat },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (targetCategory == cat) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (targetCategory == cat) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(cat.take(5), fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = autoMute, onCheckedChange = { autoMute = it })
                    Text("Auto-Mute Notifications", fontSize = 13.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = moveToSpam, onCheckedChange = { moveToSpam = it })
                    Text("Flag as Spam", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && pattern.isNotBlank()) {
                        onAdd(
                            name.trim(),
                            pattern.trim(),
                            matchField,
                            isRegex,
                            targetCategory,
                            autoMute,
                            moveToSpam,
                            assignSimSlot
                        )
                    }
                },
                enabled = name.isNotBlank() && pattern.isNotBlank()
            ) {
                Text("Add Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
