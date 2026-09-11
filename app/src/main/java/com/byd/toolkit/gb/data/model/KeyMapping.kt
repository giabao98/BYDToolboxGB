package com.byd.toolkit.gb.data.model

enum class PressType {
    SINGLE_TAP,
    DOUBLE_TAP,
    LONG_PRESS
}

enum class KeyActionType(val displayName: String) {
    PASSTHROUGH_STOCK("Mặc định nguyên bản của xe (Stock)"),
    LAUNCH_APP("Mở ứng dụng"),
    ROTATE_SCREEN("Xoay màn hình 90° (DiLink)"),
    TOGGLE_SPLIT_SCREEN("Bật/Tắt chia đôi màn hình"),
    VOICE_ASSISTANT("Gọi trợ lý giọng nói (Kiki/Google)"),
    TAKE_SCREENSHOT("Chụp ảnh màn hình"),
    STEALTH_SCREEN_OFF("Tắt màn hình (Đi đêm)"),
    NAVIGATE_HOME("Quay về màn hình chính"),
    NAVIGATE_BACK("Quay lại (Back)")
}

data class KeyAction(
    val type: KeyActionType = KeyActionType.PASSTHROUGH_STOCK,
    val targetPackage: String? = null,
    val targetAppName: String? = null
)

data class KeyConfig(
    val keyCode: Int,
    val keyName: String,
    val defaultDescription: String,
    val singleTapAction: KeyAction = KeyAction(KeyActionType.PASSTHROUGH_STOCK),
    val doubleTapAction: KeyAction? = null,
    val longPressAction: KeyAction? = null,
    val longPressDelayMs: Long = 1200L,
    val isCustomized: Boolean = false
) {
    companion object {
        const val KEYCODE_MEDIA_NEXT = 87
        const val KEYCODE_MEDIA_PREVIOUS = 88
        const val KEYCODE_BUTTON_MODE = 209
        const val KEYCODE_VOICE_ASSIST = 84
        const val KEYCODE_HEADSETHOOK = 79
        const val KEYCODE_CALL = 5

        fun defaultStockKeys(): List<KeyConfig> {
            return listOf(
                KeyConfig(
                    keyCode = KEYCODE_MEDIA_NEXT,
                    keyName = "Nút Qua Bài (Next Track)",
                    defaultDescription = "Chuyển sang bài hát tiếp theo",
                    singleTapAction = KeyAction(KeyActionType.PASSTHROUGH_STOCK),
                    doubleTapAction = null,
                    longPressAction = null,
                    isCustomized = false
                ),
                KeyConfig(
                    keyCode = KEYCODE_MEDIA_PREVIOUS,
                    keyName = "Nút Lùi Bài (Prev Track)",
                    defaultDescription = "Quay lại bài hát trước",
                    singleTapAction = KeyAction(KeyActionType.PASSTHROUGH_STOCK),
                    doubleTapAction = null,
                    longPressAction = null,
                    isCustomized = false
                ),
                KeyConfig(
                    keyCode = KEYCODE_BUTTON_MODE,
                    keyName = "Nút Chế Độ (Mode)",
                    defaultDescription = "Chuyển nguồn âm thanh (Radio/Bluetooth/USB)",
                    singleTapAction = KeyAction(KeyActionType.PASSTHROUGH_STOCK),
                    doubleTapAction = null,
                    longPressAction = null,
                    isCustomized = false
                ),
                KeyConfig(
                    keyCode = KEYCODE_VOICE_ASSIST,
                    keyName = "Nút Mic / Giọng Nói (Voice)",
                    defaultDescription = "Trợ lý mặc định DiLink",
                    singleTapAction = KeyAction(KeyActionType.PASSTHROUGH_STOCK),
                    doubleTapAction = null,
                    longPressAction = null,
                    isCustomized = false
                )
            )
        }
    }
}
