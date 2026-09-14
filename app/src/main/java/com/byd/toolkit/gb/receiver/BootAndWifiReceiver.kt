package com.byd.toolkit.gb.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.byd.toolkit.gb.BYDApplication
import com.byd.toolkit.gb.service.AppSequencerService

class BootAndWifiReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootAndWifiReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.i(TAG, "Nhận sự kiện broadcast hệ thống: $action")

        val prefs = BYDApplication.instance.preferencesRepository
        if (!prefs.isMasterServiceEnabled) {
            Log.d(TAG, "Master service tắt, bỏ qua trigger.")
            return
        }

        val config = prefs.getLaunchSequenceConfig()
        if (!config.isEnabled) {
            Log.d(TAG, "Launch sequence tắt, bỏ qua trigger.")
            return
        }

        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                if (config.triggerOnBoot) {
                    evaluateAndTrigger(context, config.requireSpecificWifi, config.targetWifiSsid)
                }
            }
            Intent.ACTION_SCREEN_ON -> {
                if (config.triggerOnScreenOn) {
                    evaluateAndTrigger(context, config.requireSpecificWifi, config.targetWifiSsid)
                }
            }
            WifiManager.NETWORK_STATE_CHANGED_ACTION,
            "android.net.conn.CONNECTIVITY_CHANGE" -> {
                if (config.requireSpecificWifi) {
                    evaluateAndTrigger(context, true, config.targetWifiSsid)
                }
            }
        }
    }

    private fun evaluateAndTrigger(context: Context, checkWifi: Boolean, targetSsid: String) {
        if (checkWifi) {
            val currentSsid = getCurrentWifiSsid(context)
            Log.i(TAG, "Kiểm tra Wi-Fi: Hiện tại='$currentSsid', Mục tiêu='$targetSsid'")

            // Clean SSID quotes if present (e.g. "MyHotspot" -> MyHotspot)
            val cleanCurrent = currentSsid?.trim('"') ?: ""
            val cleanTarget = targetSsid.trim('"')

            if (cleanCurrent.isBlank() || !cleanCurrent.contains(cleanTarget, ignoreCase = true)) {
                Log.d(TAG, "Chưa kết nối đúng Wi-Fi chỉ định, tạm hoãn tự động chạy.")
                return
            }
        }

        Log.i(TAG, "Điều kiện thỏa mãn! Khởi chạy AppSequencerService...")
        val serviceIntent = Intent(context, AppSequencerService::class.java).apply {
            action = AppSequencerService.ACTION_START_SEQUENCE
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(context, serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    private fun getCurrentWifiSsid(context: Context): String? {
        return try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            val info = wifiManager?.connectionInfo
            info?.ssid
        } catch (e: Exception) {
            null
        }
    }
}
