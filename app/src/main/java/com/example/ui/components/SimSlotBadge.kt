package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Sim1Container
import com.example.ui.theme.Sim1OnContainer
import com.example.ui.theme.Sim1Primary
import com.example.ui.theme.Sim2Container
import com.example.ui.theme.Sim2OnContainer
import com.example.ui.theme.Sim2Primary

@Composable
fun SimSlotBadge(
    slotIndex: Int,
    customName: String? = null,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val isSim2 = slotIndex == 1
    val bgColor = if (isSim2) Sim2Container else Sim1Container
    val contentColor = if (isSim2) Sim2OnContainer else Sim1OnContainer
    val primaryColor = if (isSim2) Sim2Primary else Sim1Primary
    val label = customName ?: if (isSim2) "SIM 2" else "SIM 1"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.SimCard,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(if (compact) 11.dp else 13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = if (compact) (if (isSim2) "SIM 2" else "SIM 1") else label,
                color = contentColor,
                fontSize = if (compact) 10.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
