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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MessageEntity
import com.example.ui.theme.OtpGreen
import com.example.ui.theme.OtpGreenContainer
import com.example.ui.theme.PromoSlate
import com.example.ui.theme.PromoSlateContainer
import com.example.ui.theme.SpamRed
import com.example.ui.theme.SpamRedContainer
import com.example.ui.theme.TxnOrange
import com.example.ui.theme.TxnOrangeContainer
import com.example.ui.theme.UpdateTeal
import com.example.ui.theme.UpdateTealContainer

@Composable
fun CategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, icon, label) = when (category) {
        MessageEntity.CATEGORY_OTP -> Quadruple(OtpGreenContainer, OtpGreen, Icons.Default.Security, "OTP")
        MessageEntity.CATEGORY_TRANSACTIONS -> Quadruple(TxnOrangeContainer, TxnOrange, Icons.Default.AccountBalance, "Bank")
        MessageEntity.CATEGORY_PROMOTIONS -> Quadruple(PromoSlateContainer, PromoSlate, Icons.Default.LocalOffer, "Promo")
        MessageEntity.CATEGORY_UPDATES -> Quadruple(UpdateTealContainer, UpdateTeal, Icons.Default.Notifications, "Update")
        MessageEntity.CATEGORY_SPAM -> Quadruple(SpamRedContainer, SpamRed, Icons.Default.Warning, "Spam")
        else -> Quadruple(Color(0xFFE8EAF6), Color(0xFF3F51B5), Icons.Default.Person, "Personal")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
