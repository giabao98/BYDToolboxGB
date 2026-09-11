package com.byd.toolkit.gb.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byd.toolkit.gb.ui.theme.*

@Composable
fun CockpitCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CockpitCardBorder,
    backgroundColor: Color = CockpitCard,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun CockpitButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    isDanger: Boolean = false,
    enabled: Boolean = true
) {
    val containerColor = when {
        isDanger -> CrimsonDanger
        isPrimary -> CyanPrimary
        else -> CockpitCardElevated
    }
    val contentColor = when {
        isDanger -> Color.White
        isPrimary -> TextDark
        else -> TextWhite
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 54.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = CockpitCardElevated.copy(alpha = 0.5f)
        ),
        border = if (!isPrimary && !isDanger) BorderStroke(1.dp, CockpitCardBorder) else null
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TelemetryBadge(
    label: String,
    statusColor: Color = CyanPrimary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(CockpitCardElevated)
            .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.5f)), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = TextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun TopCockpitBar(
    title: String = "BYD TOOLKIT GB",
    subtitle: String = "DiLink OS 4.0 / 5.0 • Multi-Language",
    currentLanguage: com.byd.toolkit.gb.data.model.AppLanguage = com.byd.toolkit.gb.data.model.AppLanguage.VIETNAMESE,
    onLanguageChange: (com.byd.toolkit.gb.data.model.AppLanguage) -> Unit = {},
    onEmergencyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CockpitSurface)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: BYD Brand & Title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyanPrimary.copy(alpha = 0.15f))
                    .border(1.dp, CyanPrimary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BYD",
                    color = CyanPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Center: Telemetry Badges & Language Selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TelemetryBadge(
                label = "Wi-Fi: OK",
                statusColor = CyanPrimary
            )
            TelemetryBadge(
                label = "CAN-Bus: SYNC",
                statusColor = CyanSecondary
            )

            // Language Selector Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                com.byd.toolkit.gb.data.model.AppLanguage.values().forEach { lang ->
                    val isSelected = currentLanguage == lang
                    OutlinedButton(
                        onClick = { onLanguageChange(lang) },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(34.dp),
                        border = BorderStroke(1.dp, if (isSelected) CyanPrimary else CockpitCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else Color.Transparent
                        )
                    ) {
                        Text(
                            text = lang.code.uppercase(),
                            fontSize = 11.sp,
                            color = if (isSelected) CyanPrimary else TextMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Right: Emergency Quick Recovery
        OutlinedButton(
            onClick = onEmergencyClick,
            border = BorderStroke(1.dp, AmberWarning),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberWarning)
        ) {
            Text(
                text = "⟲ Cứu Hộ / Trả Phím Zin",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}
