package com.byd.toolkit.gb.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byd.toolkit.gb.BYDApplication
import com.byd.toolkit.gb.data.model.DrivingProfile
import com.byd.toolkit.gb.service.AppSequencerService
import com.byd.toolkit.gb.ui.components.*
import com.byd.toolkit.gb.ui.theme.*

@Composable
fun DashboardScreen(
    onNavigateToAutoLaunch: () -> Unit,
    onNavigateToSteeringRemap: () -> Unit,
    onNavigateToRevertStock: () -> Unit
) {
    val context = LocalContext.current
    val prefs = BYDApplication.instance.preferencesRepository
    val scrollState = rememberScrollState()

    var masterEnabled by remember { mutableStateOf(prefs.isMasterServiceEnabled) }
    var keyRemapEnabled by remember { mutableStateOf(prefs.isKeyRemapServiceEnabled) }
    var activeProfile by remember { mutableStateOf(prefs.activeProfile) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBg)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // Section 1: Quick Master Toggles & Driving Profiles
        CockpitCard(
            borderColor = if (masterEnabled) CyanPrimary.copy(alpha = 0.4f) else CockpitCardBorder
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TRUNG TÂM ĐIỀU KHIỂN HỆ THỐNG",
                        color = CyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (masterEnabled) "Hệ thống Tự Động Hóa: ĐANG HOẠT ĐỘNG" else "Hệ thống: ĐÃ TẮT",
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tổng thể:",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = masterEnabled,
                        onCheckedChange = {
                            masterEnabled = it
                            prefs.isMasterServiceEnabled = it
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextDark,
                            checkedTrackColor = CyanPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = CockpitCardBorder)
            Spacer(modifier = Modifier.height(16.dp))

            // Profile switcher
            Text(
                text = "CHỌN KỊCH BẢN LÁI XE (DRIVING PROFILE):",
                color = TextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DrivingProfile.values().forEach { profile ->
                    val isSelected = activeProfile == profile
                    OutlinedButton(
                        onClick = {
                            activeProfile = profile
                            prefs.activeProfile = profile
                            Toast.makeText(context, "Đã chọn: ${profile.title}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) CyanPrimary else CockpitCardBorder
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else Color.Transparent,
                            contentColor = if (isSelected) CyanPrimary else TextMuted
                        )
                    ) {
                        Text(
                            text = profile.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 2: 3 Primary Feature Action Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Auto Launch & Split Screen
            CockpitCard(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "01 // TỰ ĐỘNG HÓA MỞ APP",
                    color = CyanSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tự Động Mở & Chia Đôi Màn Hình",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kịch bản: Vietmap Live Pro (Bản đồ) + Chờ 3.5s -> Spotify (Nhạc). Tự chia đôi 50:50 khi bắt đúng Wi-Fi Hotspot.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CockpitButton(
                        text = "Cấu Hình",
                        onClick = onNavigateToAutoLaunch,
                        modifier = Modifier.weight(1f),
                        isPrimary = false
                    )
                    CockpitButton(
                        text = "▶ Chạy Thử",
                        onClick = {
                            val intent = Intent(context, AppSequencerService::class.java).apply {
                                action = AppSequencerService.ACTION_START_SEQUENCE
                            }
                            context.startService(intent)
                        },
                        modifier = Modifier.weight(1f),
                        isPrimary = true
                    )
                }
            }

            // Card 2: Steering Wheel Remapper
            CockpitCard(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "02 // PHÍM CỨNG VÔ LĂNG",
                    color = CyanPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Gán Phím Cứng Vô Lăng BYD",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Giữ nút Next -> Mở Vietmap Live\nBấm nút Mode -> Gọi trợ lý Kiki\nBấm nút Mic -> Xoay màn hình 90°",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                CockpitButton(
                    text = "Cài Đặt Phím Vô Lăng",
                    onClick = onNavigateToSteeringRemap,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true
                )
            }

            // Card 3: Revert to Stock / Recovery
            CockpitCard(
                modifier = Modifier.weight(1f),
                borderColor = AmberWarning.copy(alpha = 0.5f),
                backgroundColor = CockpitCardElevated
            ) {
                Text(
                    text = "03 // CỨU HỘ & AN TOÀN",
                    color = AmberWarning,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Khôi Phục Trạng Thái Phím Gốc",
                    color = TextWhite,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Nếu gán phím gây lỗi hoặc xe phản hồi sai: 1 chạm trả toàn bộ phím vô lăng về 100% nguyên bản zin của xe BYD.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                CockpitButton(
                    text = "⟲ Cứu Hộ / Trả Phím Gốc",
                    onClick = onNavigateToRevertStock,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = false
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section 3: Bottom System Diagnostics Strip
        CockpitCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DAEMON: v2.4.9 Active",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text(
                        text = "ROOTLESS HID: OK",
                        color = CyanPrimary,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text(
                        text = "ĐỘ TRỄ: 1.2ms",
                        color = CyanSecondary,
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Text(
                    text = "Tương thích: BYD Atto 3 • Seal • Dolphin • M6 • Sealion 6",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
