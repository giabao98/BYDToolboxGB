package com.byd.toolkit.gb.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byd.toolkit.gb.R
import com.byd.toolkit.gb.data.model.AppLanguage
import com.byd.toolkit.gb.ui.components.CockpitButton
import com.byd.toolkit.gb.ui.components.CockpitCard
import com.byd.toolkit.gb.ui.components.TelemetryBadge
import com.byd.toolkit.gb.ui.theme.*

@Composable
fun PermissionGatekeeperScreen(
    hasAccessibility: Boolean,
    hasOverlay: Boolean,
    onGrantAccessibility: () -> Unit,
    onGrantOverlay: () -> Unit,
    onRecheckPermissions: () -> Unit,
    onContinue: () -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val isAllGranted = hasAccessibility && hasOverlay

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        CockpitCard(
            modifier = Modifier
                .widthIn(max = 850.dp)
                .fillMaxWidth(),
            borderColor = if (isAllGranted) CyanPrimary else CrimsonDanger,
            backgroundColor = CockpitSurface
        ) {
            // Header with language selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (isAllGranted) CyanPrimary.copy(alpha = 0.2f) else CrimsonDanger.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAllGranted) "✓" else "⚠",
                            color = if (isAllGranted) CyanPrimary else CrimsonDanger,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.permission_gatekeeper_title),
                            color = if (isAllGranted) CyanPrimary else CrimsonDanger,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isAllGranted) stringResource(R.string.permission_status_ready) else stringResource(R.string.permission_status_missing),
                            color = if (isAllGranted) CyanPrimary else CrimsonDanger,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Language Switcher Chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = currentLanguage == lang
                        OutlinedButton(
                            onClick = { onLanguageChange(lang) },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            border = BorderStroke(1.dp, if (isSelected) CyanPrimary else CockpitCardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else Color.Transparent
                            )
                        ) {
                            Text(
                                text = "${lang.flag} ${lang.label}",
                                fontSize = 11.sp,
                                color = if (isSelected) CyanPrimary else TextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.permission_gatekeeper_subtitle),
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Permission Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card 1: Accessibility
                CockpitCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = CockpitCardElevated,
                    borderColor = if (hasAccessibility) CyanPrimary.copy(alpha = 0.5f) else CrimsonDanger.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quyền 1: Trợ Năng",
                            color = if (hasAccessibility) CyanPrimary else CrimsonDanger,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TelemetryBadge(
                            label = if (hasAccessibility) stringResource(R.string.permission_granted) else stringResource(R.string.permission_not_granted),
                            statusColor = if (hasAccessibility) CyanPrimary else CrimsonDanger
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.permission_accessibility_title),
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.permission_accessibility_desc),
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (!hasAccessibility) {
                        CockpitButton(
                            text = stringResource(R.string.btn_grant_permission),
                            onClick = onGrantAccessibility,
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = true
                        )
                    } else {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
                        ) {
                            Text("✓ Đã Sẵn Sàng", color = CyanPrimary, fontSize = 13.sp)
                        }
                    }
                }

                // Card 2: Overlay
                CockpitCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = CockpitCardElevated,
                    borderColor = if (hasOverlay) CyanPrimary.copy(alpha = 0.5f) else CrimsonDanger.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quyền 2: Cửa Sổ Nổi",
                            color = if (hasOverlay) CyanPrimary else CrimsonDanger,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        TelemetryBadge(
                            label = if (hasOverlay) stringResource(R.string.permission_granted) else stringResource(R.string.permission_not_granted),
                            statusColor = if (hasOverlay) CyanPrimary else CrimsonDanger
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.permission_overlay_title),
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.permission_overlay_desc),
                        color = TextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (!hasOverlay) {
                        CockpitButton(
                            text = stringResource(R.string.btn_grant_permission),
                            onClick = onGrantOverlay,
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = true
                        )
                    } else {
                        OutlinedButton(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f))
                        ) {
                            Text("✓ Đã Sẵn Sàng", color = CyanPrimary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Quick 1-Tap ADB Action
            if (!isAllGranted) {
                CockpitCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = CyanPrimary.copy(alpha = 0.08f),
                    borderColor = CyanPrimary.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ CẤP TỰ ĐỘNG QUA LOCAL ADB (KHUYÊN DÙNG CHO XE BYD)",
                                color = CyanPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bật 'Gỡ lỗi không dây' (Port 5555) trên xe, bấm nút này để tự cấp cả Trợ Năng và Cửa Sổ Nổi mà không cần cắm PC.",
                                color = TextWhite.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        CockpitButton(
                            text = "CẤP 1-CHẠM ❯",
                            onClick = onGrantAccessibility,
                            modifier = Modifier.width(150.dp),
                            isPrimary = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onRecheckPermissions,
                        border = BorderStroke(1.dp, CockpitCardBorder),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_recheck_permission),
                            color = CyanSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!hasAccessibility && !isAllGranted) {
                        Spacer(modifier = Modifier.width(12.dp))
                        TextButton(onClick = onContinue) {
                            Text("Bỏ qua & Vào thử giao diện >", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }

                CockpitButton(
                    text = if (hasAccessibility && !hasOverlay) "VÀO ỨNG DỤNG ❯" else stringResource(R.string.btn_continue_to_app),
                    onClick = onContinue,
                    enabled = hasAccessibility || isAllGranted,
                    isPrimary = isAllGranted || hasAccessibility,
                    modifier = Modifier.width(220.dp)
                )
            }
        }
    }
}
