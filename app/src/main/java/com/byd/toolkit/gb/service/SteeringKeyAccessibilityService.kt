package com.byd.toolkit.gb.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import com.byd.toolkit.gb.BYDApplication
import com.byd.toolkit.gb.data.model.KeyAction
import com.byd.toolkit.gb.data.model.KeyActionType
import com.byd.toolkit.gb.data.model.KeyConfig

class SteeringKeyAccessibilityService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var keyDownTime: Long = 0L
    private var isLongPressTriggered = false
    private var lastClickTime: Long = 0L
    private var pendingSingleClickRunnable: Runnable? = null

    companion object {
        private const val TAG = "SteeringKeyService"
        var instance: SteeringKeyAccessibilityService? = null
            private set

        // Callback for Key Detective dialog
        var keyEventListener: ((event: KeyEvent, holdDurationMs: Long) -> Unit)? = null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.i(TAG, "BYD SteeringKeyAccessibilityService connected and ready.")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        handler.removeCallbacksAndMessages(null)
        Log.i(TAG, "BYD SteeringKeyAccessibilityService destroyed.")
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {
        // Not needed for key filtering, but required by abstract class
    }

    override fun onInterrupt() {
        Log.w(TAG, "Accessibility service interrupted.")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        // 1. Send live telemetry to Key Detective sniffer if active
        keyEventListener?.let { listener ->
            val duration = if (action == KeyEvent.ACTION_UP && keyDownTime > 0) {
                System.currentTimeMillis() - keyDownTime
            } else 0L
            listener(event, duration)
        }

        val prefs = BYDApplication.instance.preferencesRepository

        // 2. MASTER SAFEGUARD / REVERT TO STOCK CHECK:
        // If master service is off, or key remap is off, or hooks are paused -> IMMEDIATELY RETURN FALSE (OEM Passthrough)
        if (!prefs.isMasterServiceEnabled || !prefs.isKeyRemapServiceEnabled || prefs.isKeyHooksPaused) {
            return false
        }

        // Find config for this keyCode
        val keyConfig = prefs.getKeyConfigs().firstOrNull { it.keyCode == keyCode }
        if (keyConfig == null || !keyConfig.isCustomized) {
            // Unconfigured or stock key -> Passthrough to car
            return false
        }

        // 3. Handle Key Events (Down & Up)
        when (action) {
            KeyEvent.ACTION_DOWN -> {
                if (event.repeatCount == 0) {
                    keyDownTime = System.currentTimeMillis()
                    isLongPressTriggered = false

                    // Schedule long-press detection if configured
                    if (keyConfig.longPressAction != null &&
                        keyConfig.longPressAction.type != KeyActionType.PASSTHROUGH_STOCK
                    ) {
                        handler.postDelayed({
                            isLongPressTriggered = true
                            executeKeyAction(keyConfig.longPressAction, "Bấm Giữ (Long Press)")
                        }, keyConfig.longPressDelayMs)
                    }
                }
                // Consume ACTION_DOWN so native action doesn't fire prematurely
                return true
            }

            KeyEvent.ACTION_UP -> {
                // Cancel scheduled long press
                handler.removeCallbacksAndMessages(null)
                val duration = System.currentTimeMillis() - keyDownTime

                // If long press already executed, consume UP
                if (isLongPressTriggered) {
                    return true
                }

                // If long press was configured but not triggered, handle double/single tap
                val now = System.currentTimeMillis()
                val isDoubleTapCandidate = (now - lastClickTime) < 350L

                if (isDoubleTapCandidate && keyConfig.doubleTapAction != null &&
                    keyConfig.doubleTapAction.type != KeyActionType.PASSTHROUGH_STOCK
                ) {
                    // Double tap triggered
                    pendingSingleClickRunnable?.let { handler.removeCallbacks(it) }
                    pendingSingleClickRunnable = null
                    lastClickTime = 0L
                    executeKeyAction(keyConfig.doubleTapAction, "Bấm Đúp 2 Lần (Double Tap)")
                    return true
                }

                // Schedule single tap after double tap window expires
                lastClickTime = now
                pendingSingleClickRunnable?.let { handler.removeCallbacks(it) }

                if (keyConfig.doubleTapAction != null &&
                    keyConfig.doubleTapAction.type != KeyActionType.PASSTHROUGH_STOCK
                ) {
                    pendingSingleClickRunnable = Runnable {
                        executeKeyAction(keyConfig.singleTapAction, "Nhấn 1 Lần (Single Tap)")
                    }
                    handler.postDelayed(pendingSingleClickRunnable!!, 350L)
                } else {
                    // No double tap configured, execute single tap immediately
                    executeKeyAction(keyConfig.singleTapAction, "Nhấn 1 Lần (Single Tap)")
                }
                return true
            }
        }

        return super.onKeyEvent(event)
    }

    fun executeKeyAction(action: KeyAction, triggerDescription: String = "") {
        when (action.type) {
            KeyActionType.PASSTHROUGH_STOCK -> {
                // Do nothing, stock car behavior
            }
            KeyActionType.LAUNCH_APP -> {
                action.targetPackage?.let { pkg ->
                    launchApp(pkg)
                }
            }
            KeyActionType.ROTATE_SCREEN -> {
                rotateDiLinkScreen()
            }
            KeyActionType.TOGGLE_SPLIT_SCREEN -> {
                performGlobalAction(GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN)
            }
            KeyActionType.VOICE_ASSISTANT -> {
                launchVoiceAssistant()
            }
            KeyActionType.TAKE_SCREENSHOT -> {
                performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)
            }
            KeyActionType.STEALTH_SCREEN_OFF -> {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
            }
            KeyActionType.NAVIGATE_HOME -> {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
            KeyActionType.NAVIGATE_BACK -> {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
    }

    private fun launchApp(packageName: String) {
        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                startActivity(launchIntent)
            } else {
                Toast.makeText(this, "Chưa cài ứng dụng: $packageName", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi mở app $packageName", e)
        }
    }

    private fun rotateDiLinkScreen() {
        try {
            // Standard BYD DiLink screen rotate broadcast intent
            val rotateIntent = Intent("byd.intent.action.SCREEN_ROTATION")
            sendBroadcast(rotateIntent)
            Toast.makeText(this, "Đang xoay màn hình DiLink...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi gọi xoay màn hình BYD", e)
        }
    }

    private fun launchVoiceAssistant() {
        try {
            // Attempt to launch Kiki Auto if installed, otherwise system voice assist
            val kikiIntent = packageManager.getLaunchIntentForPackage("ai.zalo.kiki.auto")
            if (kikiIntent != null) {
                kikiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(kikiIntent)
            } else {
                val voiceIntent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(voiceIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Lỗi gọi trợ lý giọng nói", e)
        }
    }
}
