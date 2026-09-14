package com.byd.toolkit.gb.data.model

data class ConfigSnapshot(
    val id: String,
    val timestamp: Long,
    val title: String,
    val description: String,
    val isStockBaseline: Boolean = false,
    val keyConfigs: List<KeyConfig>,
    val launchSequence: LaunchSequenceConfig
)

enum class DrivingProfile(val title: String, val subtitle: String) {
    DAILY("Hàng ngày (Daily Drive)", "Vietmap Live + Spotify 50:50, Nút Next mở Vietmap"),
    ROADTRIP("Đường dài (Roadtrip)", "Google Maps + Vietmap cảnh báo nền, Nút Next qua bài"),
    NIGHT("Đêm (City Night)", "Zing MP3 toàn màn hình, Nút Mic tắt màn hình xe")
}
