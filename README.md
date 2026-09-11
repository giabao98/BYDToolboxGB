# BYD Toolkit GB - Car Infotainment Assistant for Vietnam

Ứng dụng Android Native chuyên biệt dành cho màn hình thông minh 12.8" & 15.6" của các dòng xe điện/hybrid **BYD tại Việt Nam** (như **BYD Atto 3, Seal, Dolphin, M6, Sealion 6, Han, Tang**) chạy hệ điều hành **BYD DiLink OS (Android 10 - 12)**.

---

## 🚀 Tính Năng Nổi Bật

### 1. Tự Động Khởi Chạy & Chia Đôi Màn Hình Thông Minh (Auto-Launch & Split-Screen)
* **Kích hoạt theo điều kiện**: Tự động mở khi nổ máy xe (`BOOT_COMPLETED`) hoặc khi bắt đúng tên **Wi-Fi Hotspot của điện thoại chủ xe** (tránh tự bật khi đỗ trong garage nhà).
* **Chuỗi mở ứng dụng tuần tự**:
  * Bước 1: Mở ứng dụng dẫn đường chính (**Vietmap Live Pro** cảnh báo phạt nguội, tốc độ).
  * Bước 2: Đếm trễ tự động (**Delay 3.5 giây**) để ứng dụng nạp xong GPS và dữ liệu giao thông.
  * Bước 3: Mở ứng dụng âm nhạc (**Spotify / Zing MP3**).
* **Chia đôi màn hình tự động (Dual Split-Screen)**:
  * Hỗ trợ màn hình ngang $50:50$ (Bản đồ bên Trái, Nhạc bên Phải).
  * Hỗ trợ màn hình xoay dọc (Bản đồ Nửa Trên, Nhạc Nửa Dưới).
  * Chế độ chạy ngầm âm thanh (Vietmap phát cảnh báo tốc độ nền, Spotify toàn màn hình).
* **Cửa sổ đếm ngược 5 giây**: Cho phép tài xế bấm **[Hủy]** nếu không muốn chạy kịch bản lúc đó.

### 2. Gán Phím Vô Lăng Đa Tầng (Steering Wheel Remapper)
* Can thiệp và gán lại các phím cứng trên vô lăng BYD:
  * **Nút Qua Bài (Next Track - Keycode 87)**:
    * *Nhấn 1 lần*: Mặc định qua bài tiếp theo (Passthrough).
    * *Nhấn đúp (2 lần)*: Chụp ảnh màn hình xe.
    * *Bấm giữ (Long press 1.2s)*: Mở nhanh **Vietmap Live Pro**.
  * **Nút Chế Độ (Mode Button - Keycode 209)**:
    * *Bấm giữ*: Gọi Trợ lý giọng nói tiếng Việt **Kiki Auto / Zalo AI**.
  * **Nút Mic / Voice (Voice Button - Keycode 84)**:
    * *Nhấn 1 lần*: Xoay màn hình DiLink 90° (Landscape ↔ Portrait).
    * *Bấm giữ*: Tắt màn hình khẩn cấp ban đêm (Stealth / Blackout Mode).
* **Bộ dò phím CAN-Bus (Key Detective)**: Công cụ bắt mã `keyCode` trực tiếp khi bấm phím trên vô lăng để tùy biến theo từng đời xe.

### 3. Cứu Hộ & Khôi Phục Trạng Thái Phím Ban Đầu (Revert to Stock & Key Recovery)
* **Nút Master Khôi Phục 1-Chạm (`⟲ KHÔI PHỤC TOÀN BỘ PHÍM VỀ ZIN XE`)**:
  * Khi bạn lỡ cấu hình sai hoặc xe bị lỗi phản hồi phím, chỉ cần 1 chạm là lập tức **giải phóng toàn bộ hook**, trả vô lăng và hệ thống về **100% nguyên bản zin của hãng BYD** xuất xưởng.
* **Tạm dừng can thiệp phím (`Pause All Key Hooks`)**: Tắt tạm thời việc can thiệp để kiểm tra phản hồi phím zin mà không bị mất cấu hình đã lưu.
* **Lịch sử Snapshot & Hoàn tác**: Tự động lưu bản sao trước mỗi lần bạn gán phím, cho phép hoàn tác về cấu hình hoạt động tốt trước đó.
* **Reset riêng từng phím**: Nếu chỉ có nút Next hoặc Mode bị lỗi, có thể bấm nút `[ Về Zin ]` riêng cho từng nút.
* **Cơ chế tự nhả phím (Self-healing Watchdog)**: Tự động nhả phím gốc nếu phát hiện độ trễ phản hồi $> 500\text{ms}$ hoặc xung đột ứng dụng.

---

## 📱 Cấu Trúc Dự Án

```
BrianBYD/
├── app/
│   ├── src/main/
│   │   ├── java/com/byd/toolkit/gb/
│   │   │   ├── BYDApplication.kt                 # Khởi tạo dữ liệu & Snapshot baseline
│   │   │   ├── MainActivity.kt                   # Điều hướng 4 màn hình chính
│   │   │   ├── data/
│   │   │   │   ├── model/KeyMapping.kt           # Định nghĩa phím, hành động, keycode
│   │   │   │   ├── model/LaunchSequence.kt       # Cấu hình chuỗi mở app & chia đôi
│   │   │   │   ├── model/SnapshotModel.kt        # Mô hình bản lưu Snapshot & Profile lái xe
│   │   │   │   └── repository/                   # SharedPreferences & Quản lý Snapshot
│   │   │   ├── service/
│   │   │   │   ├── SteeringKeyAccessibilityService.kt # Bắt sự kiện phím vô lăng & nhả phím zin
│   │   │   │   └── AppSequencerService.kt        # Dịch vụ chạy chuỗi mở app & chia đôi màn hình
│   │   │   ├── receiver/
│   │   │   │   └── BootAndWifiReceiver.kt        # Bắt sự kiện nổ máy & kết nối Wi-Fi Hotspot
│   │   │   └── ui/
│   │   │       ├── theme/                        # Bộ màu Cockpit OS (Obsidian Black & Cyan)
│   │   │       ├── components/                   # Nút bấm ô tô, Card kính mờ, Telemetry badge
│   │   │       └── screens/
│   │   │           ├── DashboardScreen.kt        # Màn hình 1: Bảng điều khiển chính
│   │   │           ├── AutoLaunchScreen.kt       # Màn hình 2: Tự động mở & Chia đôi
│   │   │           ├── SteeringRemapScreen.kt    # Màn hình 3: Gán phím vô lăng & Cân chỉnh
│   │   │           ├── RevertToStockScreen.kt    # Màn hình 4: Khôi phục phím zin & Cứu hộ
│   │   │           └── KeyDetectiveDialog.kt     # Hộp thoại dò phím CAN-bus
│   │   ├── res/
│   │   │   ├── xml/accessibility_service_config.xml
│   │   │   └── values/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 🛠 Hướng Dẫn Biên Dịch File APK

Mở thư mục dự án bằng **Android Studio** hoặc chạy lệnh dòng lệnh (yêu cầu JDK 17):

```bash
# Build file APK Debug:
./gradlew assembleDebug

# File APK sau khi build sẽ nằm tại:
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🚗 Hướng Dẫn Cài Đặt Lên Xe BYD

1. Chép file `app-debug.apk` vào ổ đĩa **USB Flash Drive (định dạng FAT32)**.
2. Cắm USB vào cổng dữ liệu trên xe BYD.
3. Mở ứng dụng **Trình Quản Lý Tệp (File Manager)** trên màn hình DiLink, chọn file APK và bấm **Cài đặt**.
4. Sau khi cài xong, mở app và cấp 2 quyền hệ thống:
   * **Hỗ Trợ Tiếp Cận (Accessibility Service)**: Để app bắt được tín hiệu phím vô lăng và chia đôi màn hình.
   * **Hiển Thị Trên Ứng Dụng Khác (Display Over Other Apps)**: Để hiển thị đếm ngược 5 giây khi khởi động xe.
5. Thiết lập tên Wi-Fi Hotspot của điện thoại bạn và tận hưởng trải nghiệm lái xe tự động hoàn toàn!
