package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.SimProfileEntity
import com.example.ui.theme.Sim1Container
import com.example.ui.theme.Sim1OnContainer
import com.example.ui.theme.Sim1Primary
import com.example.ui.theme.Sim2Container
import com.example.ui.theme.Sim2OnContainer
import com.example.ui.theme.Sim2Primary

@Composable
fun NewMessageDialog(
    simProfiles: List<SimProfileEntity>,
    onDismiss: () -> Unit,
    onSend: (recipient: String, text: String, simSlot: Int) -> Unit
) {
    var recipient by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var selectedSimSlot by remember { mutableIntStateOf(0) }

    val sim1 = simProfiles.firstOrNull { it.slotIndex == 0 }
    val sim2 = simProfiles.firstOrNull { it.slotIndex == 1 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("New Dual SIM Message", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Outbound SIM Selector Bar
                Text(
                    text = "Send via:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // SIM 1 Chip
                    val isSim1Selected = selectedSimSlot == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSim1Selected) Sim1Primary else Sim1Container)
                            .clickable { selectedSimSlot = 0 }
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SimCard,
                                contentDescription = null,
                                tint = if (isSim1Selected) Color.White else Sim1OnContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sim1?.customName ?: "SIM 1 (Personal)",
                                color = if (isSim1Selected) Color.White else Sim1OnContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    // SIM 2 Chip
                    val isSim2Selected = selectedSimSlot == 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSim2Selected) Sim2Primary else Sim2Container)
                            .clickable { selectedSimSlot = 1 }
                            .padding(vertical = 8.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SimCard,
                                contentDescription = null,
                                tint = if (isSim2Selected) Color.White else Sim2OnContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sim2?.customName ?: "SIM 2 (Work)",
                                color = if (isSim2Selected) Color.White else Sim2OnContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("To (Phone number or contact)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Message text") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val chars = messageText.length
                    val parts = if (chars == 0) 1 else ((chars - 1) / 160) + 1
                    Text(
                        text = "$chars chars ($parts SMS)",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (recipient.isNotBlank() && messageText.isNotBlank()) {
                        onSend(recipient.trim(), messageText.trim(), selectedSimSlot)
                        onDismiss()
                    }
                },
                enabled = recipient.isNotBlank() && messageText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSimSlot == 1) Sim2Primary else Sim1Primary
                )
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
