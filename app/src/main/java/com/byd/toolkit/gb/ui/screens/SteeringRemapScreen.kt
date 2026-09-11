package com.byd.toolkit.gb.ui.screens

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byd.toolkit.gb.BYDApplication
import com.byd.toolkit.gb.data.model.KeyAction
import com.byd.toolkit.gb.data.model.KeyActionType
import com.byd.toolkit.gb.data.model.KeyConfig
import com.byd.toolkit.gb.ui.components.*
import com.byd.toolkit.gb.ui.theme.*

@Composable
fun SteeringRemapScreen(
    onBackClick: () -> Unit,
    onOpenKeyDetective: () -> Unit
) {
    val context = LocalContext.current
    val prefs = BYDApplication.instance.preferencesRepository
    val snapshotRepo = BYDApplication.instance.snapshotRepository
    val scrollState = rememberScrollState()

    var keyConfigs by remember { mutableStateOf(prefs.getKeyConfigs()) }
    var remapEnabled by remember { mutableStateOf(prefs.isKeyRemapServiceEnabled) }
    var longPressDelay by remember { mutableStateOf(1.2f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBg)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = onBackClick,
                    border = BorderStroke(1.dp, CockpitCardBorder),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("❮ Dashboard", color = CyanPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Gán Phím Cứng Vô Lăng BYD",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tùy biến thao tác Bấm 1 lần, Bấm đúp 2 lần hoặc Bấm giữ trên vô lăng",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CockpitButton(
                    text = "🔍 Dò Phím Vô Lăng",
                    onClick = onOpenKeyDetective,
                    isPrimary = false
                )
                CockpitButton(
                    text = "✓ Lưu Cấu Hình",
                    onClick = {
                        // Create snapshot before saving
                        snapshotRepo.createSnapshot(
                            title = "Cấu hình gán phím tùy biến",
                            description = "Lưu lúc người dùng chỉnh sửa cấu hình phím vô lăng"
                        )
                        prefs.saveKeyConfigs(keyConfigs)
                        prefs.isKeyRemapServiceEnabled = remapEnabled
                        Toast.makeText(context, "Đã lưu cấu hình phím vô lăng & tạo bản sao lưu!", Toast.LENGTH_SHORT).show()
                    },
                    isPrimary = true
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3-Column Layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Column 1: Steering Wheel Visual Map
            CockpitCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = "01 // SƠ ĐỒ VÔ LĂNG BYD",
                    color = CyanSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(CockpitCardElevated, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("⌾ VÔ LĂNG THỂ THAO D-CUT", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Cụm phím Phải:\n• Nút Qua Bài (Next Track)\n• Nút Lùi Bài (Prev Track)\n• Nút Mode (Chuyển chế độ)\n• Nút Mic (Voice)\n• Con lăn chỉnh âm lượng",
                            color = TextWhite,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Can thiệp phím vô lăng:", color = TextWhite, fontSize = 14.sp)
                    Switch(
                        checked = remapEnabled,
                        onCheckedChange = { remapEnabled = it }
                    )
                }
            }

            // Column 2: Key Mapping Items
            CockpitCard(modifier = Modifier.weight(1.3f)) {
                Text(
                    text = "02 // DANH SÁCH PHÍM & LỆNH GÁN",
                    color = CyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                keyConfigs.forEachIndexed { index, config ->
                    CockpitCard(
                        modifier = Modifier.padding(bottom = 10.dp),
                        backgroundColor = CockpitCardElevated,
                        borderColor = if (config.isCustomized) CyanPrimary.copy(alpha = 0.5f) else CockpitCardBorder
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(config.keyName, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Keycode: ${config.keyCode} • ${config.defaultDescription}", color = TextMuted, fontSize = 11.sp)
                            }

                            // Quick preset toggle for demo/use
                            OutlinedButton(
                                onClick = {
                                    val updatedList = keyConfigs.toMutableList()
                                    val newConfig = when (config.keyCode) {
                                        KeyConfig.KEYCODE_MEDIA_NEXT -> config.copy(
                                            longPressAction = KeyAction(KeyActionType.LAUNCH_APP, "vn.vietmap.live", "Vietmap Live Pro"),
                                            doubleTapAction = KeyAction(KeyActionType.TAKE_SCREENSHOT),
                                            isCustomized = true
                                        )
                                        KeyConfig.KEYCODE_BUTTON_MODE -> config.copy(
                                            longPressAction = KeyAction(KeyActionType.VOICE_ASSISTANT, targetAppName = "Kiki AI"),
                                            isCustomized = true
                                        )
                                        KeyConfig.KEYCODE_VOICE_ASSIST -> config.copy(
                                            singleTapAction = KeyAction(KeyActionType.ROTATE_SCREEN),
                                            longPressAction = KeyAction(KeyActionType.STEALTH_SCREEN_OFF),
                                            isCustomized = true
                                        )
                                        else -> config
                                    }
                                    updatedList[index] = newConfig
                                    keyConfigs = updatedList
                                    Toast.makeText(context, "Đã áp dụng mẫu gán cho ${config.keyName}!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, CyanPrimary)
                            ) {
                                Text(if (config.isCustomized) "Đã Gán" else "+ Gán Nhanh", fontSize = 12.sp, color = CyanPrimary)
                            }
                        }

                        if (config.isCustomized) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = CockpitCardBorder)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("• Bấm 1 lần: ${config.singleTapAction.type.displayName}", color = TextMuted, fontSize = 12.sp)
                            config.doubleTapAction?.let {
                                Text("• Bấm đúp: ${it.type.displayName}", color = CyanSecondary, fontSize = 12.sp)
                            }
                            config.longPressAction?.let {
                                Text("• Giữ lâu: ${it.type.displayName} (${it.targetAppName ?: ""})", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Column 3: Calibration & Sensitivity
            CockpitCard(modifier = Modifier.weight(0.9f)) {
                Text(
                    text = "03 // CÂN CHỈNH ĐỘ NHẠY",
                    color = AmberWarning,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Thời gian bấm giữ (Long Press):", color = TextMuted, fontSize = 12.sp)
                    Text("${"%.1f".format(longPressDelay)}s", color = AmberWarning, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = longPressDelay,
                    onValueChange = { longPressDelay = it },
                    valueRange = 0.8f..2.5f,
                    steps = 17,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberWarning,
                        activeTrackColor = AmberWarning
                    )
                )

                Text(
                    text = "Khuyên dùng 1.2s để tránh kích hoạt nhầm khi vần vô lăng vào cua.",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                TelemetryBadge(
                    label = "Tự nhả phím: BẬT (Tránh kẹt)",
                    statusColor = CyanPrimary
                )
            }
        }
    }
}
