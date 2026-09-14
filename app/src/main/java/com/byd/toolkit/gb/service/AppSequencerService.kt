package com.byd.toolkit.gb.service

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.byd.toolkit.gb.BYDApplication
import com.byd.toolkit.gb.data.model.SplitMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppSequencerService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    companion object {
        private const val TAG = "AppSequencerService"
        private const val CHANNEL_ID = "byd_sequencer_channel"
        private const val NOTIFICATION_ID = 4209

        const val ACTION_START_SEQUENCE = "com.byd.toolkit.ACTION_START_SEQUENCE"
        const val ACTION_CANCEL_SEQUENCE = "com.byd.toolkit.ACTION_CANCEL_SEQUENCE"
        var isSequenceRunning = false
            private set
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CANCEL_SEQUENCE -> {
                cancelSequence()
                stopSelf()
            }
            ACTION_START_SEQUENCE, null -> {
                startForeground(NOTIFICATION_ID, buildNotification("Đang chạy chuỗi khởi động ứng dụng BYD..."))
                executeLaunchSequence()
            }
        }
        return START_NOT_STICKY
    }

    private fun executeLaunchSequence() {
        val prefs = BYDApplication.instance.preferencesRepository
        if (!prefs.isMasterServiceEnabled) {
            stopSelf()
            return
        }

        val config = prefs.getLaunchSequenceConfig()
        if (!config.isEnabled) {
            stopSelf()
            return
        }

        isSequenceRunning = true

        serviceScope.launch {
            try {
                Log.i(TAG, "Bước 1: Mở ứng dụng 1: ${config.firstApp.appName} (${config.firstApp.packageName})")
                launchApp(config.firstApp.packageName)

                // Step 2: Buffer Delay
                val delayMs = (config.delaySeconds * 1000).toLong()
                Log.i(TAG, "Bước 2: Chờ độ trễ ${config.delaySeconds}s...")
                delay(delayMs)

                // Step 3: Handle Split Screen or App 2
                val app2 = config.secondApp
                if (app2 != null && app2.packageName.isNotBlank()) {
                    when (config.splitMode) {
                        SplitMode.LANDSCAPE_50_50, SplitMode.PORTRAIT_VERTICAL -> {
                            Log.i(TAG, "Bước 3: Kích hoạt chia đôi màn hình và mở app 2: ${app2.appName}")
                            triggerSplitScreen()
                            delay(800)
                            launchApp(app2.packageName)
                        }
                        SplitMode.STEALTH_AUDIO_BACKGROUND -> {
                            Log.i(TAG, "Bước 3: Mở toàn màn hình app 2: ${app2.appName}")
                            launchApp(app2.packageName)
                        }
                        SplitMode.SINGLE_APP_FULLSCREEN -> {
                            // Only app 1 is needed
                        }
                    }
                }

                Toast.makeText(
                    this@AppSequencerService,
                    "BYD Toolkit GB: Đã kích hoạt kịch bản lái xe!",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {
                Log.e(TAG, "Lỗi thực thi chuỗi mở app", e)
            } finally {
                isSequenceRunning = false
                stopSelf()
            }
        }
    }

    private fun launchApp(packageName: String) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                )
                startActivity(launchIntent)
            } else {
                Log.w(TAG, "Không tìm thấy gói ứng dụng: $packageName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi mở app $packageName", e)
        }
    }

    private fun triggerSplitScreen() {
        val accessibility = SteeringKeyAccessibilityService.instance
        if (accessibility != null) {
            accessibility.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
        } else {
            Log.w(TAG, "AccessibilityService chưa được bật để chia đôi màn hình tự động.")
        }
    }

    private fun cancelSequence() {
        isSequenceRunning = false
        Log.i(TAG, "Tài xế đã hủy chuỗi khởi chạy ứng dụng.")
        Toast.makeText(this, "Đã hủy tự động mở ứng dụng.", Toast.LENGTH_SHORT).show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "BYD Toolkit Automation Channel",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Kênh thông báo tự động hóa khởi chạy xe BYD"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BYD Toolkit GB")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
