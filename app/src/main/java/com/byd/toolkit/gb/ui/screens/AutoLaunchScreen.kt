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
import com.byd.toolkit.gb.data.model.AppItem
import com.byd.toolkit.gb.data.model.LaunchSequenceConfig
import com.byd.toolkit.gb.data.model.SplitMode
import com.byd.toolkit.gb.service.AppSequencerService
import com.byd.toolkit.gb.ui.components.*
import com.byd.toolkit.gb.ui.theme.*

@Composable
fun AutoLaunchScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val prefs = BYDApplication.instance.preferencesRepository
    val scrollState = rememberScrollState()

    var config by remember { mutableStateOf(prefs.getLaunchSequenceConfig()) }
    var wifiSsidText by remember { mutableStateOf(config.targetWifiSsid) }
    var delayValue by remember { mutableStateOf(config.delaySeconds) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBg)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // Header
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
                        text = "Cấu Hình Tự Động Khởi Chạy & Chia Đôi",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Thiết lập kịch bản tự động mở Vietmap Live & Spotify khi kết nối Wi-Fi",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            CockpitButton(
                text = "✓ Lưu Cấu Hình",
                onClick = {
                    val updated = config.copy(
                        targetWifiSsid = wifiSsidText,
                        delaySeconds = delayValue
                    )
                    prefs.saveLaunchSequenceConfig(updated)
                    config = updated
                    Toast.makeText(context, "Đã lưu cấu hình tự động mở app!", Toast.LENGTH_SHORT).show()
                },
                isPrimary = true
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3 Columns Layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Column 1: Trigger Conditions
            CockpitCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = "01 // ĐIỀU KIỆN KÍCH HOẠT (TRIGGERS)",
                    color = CyanSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Toggle: Bật tính năng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bật tự động mở app:", color = TextWhite, fontSize = 14.sp)
                    Switch(
                        checked = config.isEnabled,
                        onCheckedChange = { config = config.copy(isEnabled = it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle: Boot Completed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Khi xe khởi động (Boot):", color = TextWhite, fontSize = 14.sp)
                    Switch(
                        checked = config.triggerOnBoot,
                        onCheckedChange = { config = config.copy(triggerOnBoot = it) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Toggle: Require Specific Wi-Fi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chỉ khi bắt đúng Wi-Fi:", color = TextWhite, fontSize = 14.sp)
                    Switch(
                        checked = config.requireSpecificWifi,
                        onCheckedChange = { config = config.copy(requireSpecificWifi = it) }
                    )
                }

                if (config.requireSpecificWifi) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = wifiSsidText,
                        onValueChange = { wifiSsidText = it },
                        label = { Text("Tên Wi-Fi Hotspot điện thoại") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ví dụ: Tên Hotspot iPhone hoặc điện thoại Android của bạn.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Column 2: Launch Orchestration Pipeline
            CockpitCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = "02 // KỊCH BẢN CHUỖI KHỞI CHẠY",
                    color = CyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Step 1: App 1
                Text("Bước 1: Ứng dụng dẫn đường chính", color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                CockpitCard(backgroundColor = CockpitCardElevated) {
                    Text(config.firstApp.appName, color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Gói: ${config.firstApp.packageName}", color = TextMuted, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step 2: Delay Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bước 2: Chờ độ trễ:", color = TextMuted, fontSize = 12.sp)
                    Text("${"%.1f".format(delayValue)} giây", color = CyanPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Slider(
                    value = delayValue,
                    onValueChange = { delayValue = it },
                    valueRange = 1.0f..8.0f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanPrimary,
                        activeTrackColor = CyanPrimary
                    )
                )
                Text(
                    text = "Khuyến nghị 3.0s - 4.0s để Vietmap Live nạp xong GPS trước khi mở nhạc.",
                    color = TextMuted,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Step 3: App 2
                Text("Bước 3: Ứng dụng giải trí / nghe nhạc", color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                CockpitCard(backgroundColor = CockpitCardElevated) {
                    Text(config.secondApp?.appName ?: "Chưa chọn", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Gói: ${config.secondApp?.packageName ?: "N/A"}", color = TextMuted, fontSize = 11.sp)
                }
            }

            // Column 3: Split Modes & Run Simulation
            CockpitCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = "03 // CHẾ ĐỘ CHIA ĐÔI MÀN HÌNH BYD",
                    color = AmberWarning,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                SplitMode.values().forEach { mode ->
                    val isSelected = config.splitMode == mode
                    OutlinedButton(
                        onClick = { config = config.copy(splitMode = mode) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSelected) CyanPrimary else CockpitCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.12f) else Color.Transparent
                        )
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(
                                text = mode.title,
                                color = if (isSelected) CyanPrimary else TextWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = mode.description,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                CockpitButton(
                    text = "▶ CHẠY THỬ NGHIỆM NGAY",
                    onClick = {
                        val intent = Intent(context, AppSequencerService::class.java).apply {
                            action = AppSequencerService.ACTION_START_SEQUENCE
                        }
                        context.startService(intent)
                        Toast.makeText(context, "Bắt đầu chạy chuỗi mở app thử nghiệm!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = true
                )
            }
        }
    }
}
