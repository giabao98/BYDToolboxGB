package com.byd.toolkit.gb.ui.screens

import android.view.KeyEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.byd.toolkit.gb.service.SteeringKeyAccessibilityService
import com.byd.toolkit.gb.ui.components.CockpitButton
import com.byd.toolkit.gb.ui.components.CockpitCard
import com.byd.toolkit.gb.ui.theme.*

@Composable
fun KeyDetectiveDialog(
    onDismiss: () -> Unit
) {
    var detectedKeyCode by remember { mutableStateOf<Int?>(null) }
    var detectedKeyName by remember { mutableStateOf<String?>(null) }
    var holdDuration by remember { mutableStateOf(0L) }
    var eventCount by remember { mutableStateOf(0) }

    // Register callback with SteeringKeyAccessibilityService
    DisposableEffect(Unit) {
        SteeringKeyAccessibilityService.keyEventListener = { event, duration ->
            detectedKeyCode = event.keyCode
            detectedKeyName = KeyEvent.keyCodeToString(event.keyCode)
            holdDuration = duration
            eventCount++
        }

        onDispose {
            SteeringKeyAccessibilityService.keyEventListener = null
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        CockpitCard(
            modifier = Modifier.width(550.dp),
            borderColor = CyanPrimary,
            backgroundColor = CockpitCardElevated
        ) {
            Text(
                text = "BỘ DÒ PHÍM CAN-BUS (KEY DETECTIVE)",
                color = CyanPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Bấm hoặc giữ phím vật lý bất kỳ trên vô lăng để hệ thống nhận diện mã phím:",
                color = TextWhite,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sniffer Display Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(CockpitBg, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (detectedKeyCode == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = CyanPrimary,
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Đang lắng nghe tín hiệu vô lăng...",
                            color = TextMuted,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MÃ PHÍM BẮT ĐƯỢC:",
                            color = CyanSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${detectedKeyName ?: "KEYCODE"} (${detectedKeyCode})",
                            color = CyanPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Thời gian giữ: ${holdDuration}ms • Sự kiện: #$eventCount",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                CockpitButton(
                    text = "Đóng",
                    onClick = onDismiss,
                    isPrimary = true
                )
            }
        }
    }
}
