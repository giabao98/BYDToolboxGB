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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.byd.toolkit.gb.BYDApplication
import com.byd.toolkit.gb.ui.components.*
import com.byd.toolkit.gb.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RevertToStockScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val prefs = BYDApplication.instance.preferencesRepository
    val snapshotRepo = BYDApplication.instance.snapshotRepository
    val scrollState = rememberScrollState()

    var isPaused by remember { mutableStateOf(prefs.isKeyHooksPaused) }
    var selfHealing by remember { mutableStateOf(prefs.isSelfHealingEnabled) }
    var keyConfigs by remember { mutableStateOf(prefs.getKeyConfigs()) }
    var snapshots by remember { mutableStateOf(snapshotRepo.getSnapshots()) }

    val dateFormat = remember { SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CockpitBg)
            .verticalScroll(scrollState)
            .padding(20.dp)
    ) {
        // Top Header
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
                        text = "Khôi Phục Trạng Thái Phím Ban Đầu (Revert to Stock)",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cứu hộ an toàn: Trả toàn bộ phím vô lăng về 100% nguyên bản zin của xe BYD",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            TelemetryBadge(
                label = "TRẠNG THÁI: SẴN SÀNG HOÀN TÁC",
                statusColor = CyanPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3-Column Layout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Column 1: Master Revert to Stock
            CockpitCard(
                modifier = Modifier.weight(1f),
                borderColor = AmberWarning.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "01 // TRẢ VỀ NGUYÊN BẢN XE BAN ĐẦU",
                    color = AmberWarning,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Nếu bạn gán phím gây ra lỗi, xung đột hoặc xe phản hồi không như ý muốn, hãy bấm nút dưới đây:",
                    color = TextWhite,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Huge Master Button
                CockpitButton(
                    text = "⟲ KHÔI PHỤC TOÀN BỘ PHÍM VỀ ZIN XE",
                    onClick = {
                        snapshotRepo.restoreFullStockCarState()
                        keyConfigs = prefs.getKeyConfigs()
                        isPaused = false
                        Toast.makeText(
                            context,
                            "Thành công! Đã trả toàn bộ phím vô lăng về 100% nguyên bản của hãng BYD.",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(65.dp),
                    isPrimary = true
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = CockpitCardBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Toggle: Pause All Key Hooks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tạm dừng can thiệp phím:", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Tắt tạm thời hook để thử phím zin mà không xóa cấu hình", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = isPaused,
                        onCheckedChange = {
                            isPaused = it
                            prefs.isKeyHooksPaused = it
                            Toast.makeText(
                                context,
                                if (it) "Đã tạm dừng hook phím (Phím xe chạy zin)" else "Đã bật lại hook phím",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "✓ 100% An Toàn: Không sửa file hệ thống, không chạm bootloader, bảo toàn phím ADAS và bảo hành của xe.",
                    color = CyanSecondary,
                    fontSize = 11.sp
                )
            }

            // Column 2: Snapshots & Rollback History
            CockpitCard(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = "02 // LỊCH SỬ CẤU HÌNH & HOÀN TÁC (SNAPSHOTS)",
                    color = CyanPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Hệ thống tự động lưu bản sao lưu trước mỗi lần thay đổi gán phím:",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (snapshots.isEmpty()) {
                    Text("Chưa có bản sao lưu nào.", color = TextMuted, fontSize = 13.sp)
                } else {
                    snapshots.forEach { snapshot ->
                        CockpitCard(
                            modifier = Modifier.padding(bottom = 8.dp),
                            backgroundColor = CockpitCardElevated,
                            borderColor = if (snapshot.isStockBaseline) AmberWarning.copy(alpha = 0.5f) else CockpitCardBorder
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = snapshot.title,
                                        color = if (snapshot.isStockBaseline) AmberWarning else TextWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${dateFormat.format(Date(snapshot.timestamp))} • ${snapshot.description}",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        snapshotRepo.rollbackToSnapshot(snapshot.id)
                                        keyConfigs = prefs.getKeyConfigs()
                                        Toast.makeText(context, "Đã hoàn tác về: ${snapshot.title}", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, CyanPrimary)
                                ) {
                                    Text("Hoàn Tác", color = CyanPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Column 3: Individual Key Reset & Clean Wipe
            CockpitCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = "03 // RESET RIÊNG TỪNG PHÍM",
                    color = CyanSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text("Nếu chỉ 1 phím bị lỗi, bạn có thể đưa riêng phím đó về zin:", color = TextMuted, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(10.dp))

                keyConfigs.forEach { config ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = config.keyName,
                            color = TextWhite,
                            fontSize = 13.sp
                        )

                        OutlinedButton(
                            onClick = {
                                prefs.resetSingleKeyToStock(config.keyCode)
                                keyConfigs = prefs.getKeyConfigs()
                                Toast.makeText(context, "Đã trả ${config.keyName} về phím zin!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CockpitCardBorder)
                        ) {
                            Text("Về Zin", color = AmberWarning, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = CockpitCardBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Danger clean reset
                Text("Xóa trắng toàn bộ dữ liệu app:", color = CrimsonDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                CockpitButton(
                    text = "⚠ Xóa Trắng Cấu Hình Về Mặc Định",
                    onClick = {
                        prefs.clearAllData()
                        snapshotRepo.restoreFullStockCarState()
                        keyConfigs = prefs.getKeyConfigs()
                        snapshots = snapshotRepo.getSnapshots()
                        Toast.makeText(context, "Đã xóa trắng toàn bộ dữ liệu app!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isDanger = true
                )
            }
        }
    }
}
