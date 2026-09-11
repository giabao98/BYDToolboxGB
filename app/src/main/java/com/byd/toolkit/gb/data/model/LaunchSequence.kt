package com.byd.toolkit.gb.data.model

enum class SplitMode(val title: String, val description: String) {
    LANDSCAPE_50_50(
        "Màn hình ngang 50:50",
        "Chia đôi đều 2 ứng dụng: Bản đồ bên Trái, Âm nhạc bên Phải"
    ),
    PORTRAIT_VERTICAL(
        "Màn hình dọc Xoay BYD",
        "Khi xoay dọc màn hình: Bản đồ ở Nửa Trên, Âm nhạc ở Nửa Dưới"
    ),
    STEALTH_AUDIO_BACKGROUND(
        "Chạy ngầm cảnh báo tốc độ",
        "App 1 (Vietmap Live) chạy ngầm phát giọng nói cảnh báo phạt nguội, App 2 chiếm toàn màn hình"
    ),
    SINGLE_APP_FULLSCREEN(
        "Chỉ mở App 1 Toàn Màn Hình",
        "Mở duy nhất ứng dụng dẫn đường chính khi khởi động xe"
    )
}

data class AppItem(
    val packageName: String,
    val appName: String,
    val isAvailable: Boolean = true
)

data class LaunchSequenceConfig(
    val isEnabled: Boolean = true,
    val triggerOnBoot: Boolean = true,
    val triggerOnScreenOn: Boolean = false,
    val requireSpecificWifi: Boolean = true,
    val targetWifiSsid: String = "Brian_iPhone_15_Pro_Hotspot",
    val suppressIfDriving: Boolean = true,
    val firstApp: AppItem = AppItem("vn.vietmap.live", "Vietmap Live Pro"),
    val delaySeconds: Float = 3.5f,
    val secondApp: AppItem? = AppItem("com.spotify.music", "Spotify"),
    val splitMode: SplitMode = SplitMode.LANDSCAPE_50_50,
    val showCancelCountdown: Boolean = true,
    val countdownDurationSeconds: Int = 5,
    val autoRelaunchIfCrash: Boolean = true
)
